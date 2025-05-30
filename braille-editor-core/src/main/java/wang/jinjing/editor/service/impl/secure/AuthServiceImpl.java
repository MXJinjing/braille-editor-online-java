package wang.jinjing.editor.service.impl.secure;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wang.jinjing.common.exception.ServiceException;
import wang.jinjing.common.util.BeanConvertUtil;
import wang.jinjing.editor.pojo.VO.EditorUserVO;
import wang.jinjing.editor.pojo.VO.LoginResponseVO;
import wang.jinjing.editor.pojo.entity.EditorUser;
import wang.jinjing.common.pojo.ErrorEnum;
import wang.jinjing.editor.repository.EditorUserRepository;
import wang.jinjing.editor.repository.impl.EditorUserRepositoryImpl;
import wang.jinjing.editor.service.file.BaseFileService;
import wang.jinjing.editor.service.oss.S3BucketService;
import wang.jinjing.editor.service.secure.AuthService;
import wang.jinjing.editor.service.RedisService;
import wang.jinjing.editor.util.JwtUtils;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private EditorUserRepository repository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private BaseFileService baseFileService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Value("${captcha.expiration.secs}")
    private static final long CAPTCHA_EXPIRATION = 30;

    @Value("${jwt.expiration.ms}")
    private static final long USER_CACHE_EXPIRATION = 86400000;
    @Autowired
    private EditorUserRepositoryImpl editorUserRepositoryImpl;


    @Override
    @Transactional
    public LoginResponseVO authenticateLogin(String username, String password) {

        // 使用ProviderManager auth方法进行验证
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

        // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername

     try {
            Authentication authenticate = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

            // 如果校验失败了
            if (Objects.isNull(authenticate)) {
                throw new ServiceException("");
            }
            EditorUser userDetails = (EditorUser) (authenticate.getPrincipal());
            // 自己生成jwt token给前端
            String token = jwtUtils.generateJwtToken(authenticate);
            Date expirationDateFromToken = jwtUtils.getExpirationDateFromToken(token);

            // 系统用户token放入redis
            redisService.setCacheObject("LOGIN_TOKEN_KEY" + username + "::", token, USER_CACHE_EXPIRATION , TimeUnit.MICROSECONDS);

            // 系统用户信息放入redis
            redisService.setCacheObject("USER_INFO_KEY" + username + "::", userDetails, USER_CACHE_EXPIRATION, TimeUnit.MICROSECONDS);

            System.out.println(userDetails);

            // 返回给前端
            editorUserRepositoryImpl.updateSingle(userDetails.getId(), "last_login_at", new Date());
            return new LoginResponseVO("Bearer " + token, userDetails.getUsername(), expirationDateFromToken);
        } catch (Exception e) {
            throw new ServiceException(ErrorEnum.USERNAME_OR_PASSWORD_ERROR,e);
        }

    }

    @Override
    public LoginResponseVO refreshToken(String token) {
        return null;
    }

    @Override
    public boolean verifyToken(String token) {
        return false;
    }

    @Override
    public UserDetails getUserInfo(String token) {
        String usernameFromJwtToken = jwtUtils.getUsernameFromJwtToken(token);
        return  userDetailsService.loadUserByUsername(usernameFromJwtToken);
    }

    /**
     *
     * @param username
     * @param newPassword
     * @param oldPassword
     * @return
     */
    @Transactional
    @Override
    public int changePassword(String username, String newPassword, String oldPassword) {
        // 使用AuthenticationManager进行密码验证
        EditorUser editorUser = (EditorUser) userDetailsService.loadUserByUsername(username);
        Long userId = editorUser.getId();

        // 验证密码
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, oldPassword);
        authentication = authenticationManager.authenticate(authentication);

        if (Objects.isNull(authentication)) {
            throw new ServiceException("用户名或密码错误");
        } else {
            // 更新密码
            String encodePassword = passwordEncoder.encode(newPassword);
            return repository.updateSingle(userId, "password", encodePassword);
        }
    }

    /**
     *
     * @param username
     * @param newPassword
     * @param emailCode
     * @return
     */
    @Override
    @Transactional
    public int resetPassword(String username, String newPassword, String emailCode) {
        EditorUser editorUser = (EditorUser) userDetailsService.loadUserByUsername(username);
        Long userId = editorUser.getId();
        // 检查邮箱验证码
        String emailCodeKey = "EMAIL_CODE_KEY:" + userId + "RESET_PASSWORD";

        if (!redisService.hasKey(emailCodeKey)) {
            throw new ServiceException(ErrorEnum.EMAIL_CODE_EXPIRED);
        } else{
            String code = redisService.get(emailCodeKey);
            if (!code.equals(emailCode)) {
                throw new ServiceException(ErrorEnum.EMAIL_CODE_ERROR);
            } else {
                // 更新密码
                String encodePassword = passwordEncoder.encode(newPassword);
                return repository.updateSingle(userId, "password", encodePassword);
            }
        }
    }

    @Autowired
    private ThreadPoolTaskExecutor customTaskExecutor;

    @Override
    @Transactional
    public EditorUserVO registerUser(String username, String email, String password) {
        // 检测用户名是否存在
        EditorUser editorUser1 = repository.selectByUsername(username);
        if(!Objects.isNull(editorUser1)){
            throw new ServiceException(ErrorEnum.USER_NAME_ALREADY_EXIST);
        }

        String encodePassword = passwordEncoder.encode(password);

        EditorUser editorUser = new EditorUser();
        editorUser.setUsername(username);
        editorUser.setNickname(username);
        editorUser.setEmail(email);
        editorUser.setPassword(encodePassword);

        repository.insert(editorUser);

        // 进行异步初始化桶
        CompletableFuture.runAsync(() -> {
            System.out.println("==进行异步初始化==");
            baseFileService.initBucket("user-"+editorUser.getId(),editorUser);
            System.out.println("==完成异步初始化==");
        }, customTaskExecutor).exceptionally(ex -> {
            System.err.println("初始化桶异常: " + ex.getCause().getMessage());
            // 返回默认值或执行补偿逻辑
            return null;
        });

        EditorUserVO editorUserVO = BeanConvertUtil.convertToVo(EditorUserVO.class,editorUser);
        return editorUserVO;
    }


}
