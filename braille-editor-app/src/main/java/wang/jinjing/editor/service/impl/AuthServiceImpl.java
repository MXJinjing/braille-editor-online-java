package wang.jinjing.editor.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import wang.jinjing.common.exception.AuthServiceException;
import wang.jinjing.editor.pojo.VO.LoginResponseVO;
import wang.jinjing.editor.pojo.entity.EditorUser;
import wang.jinjing.editor.service.AuthService;
import wang.jinjing.editor.service.RedisService;
import wang.jinjing.editor.util.JwtUtils;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${captcha.expiration.secs}")
    private static final long CAPTCHA_EXPIRATION = 30;

    @Override
    public LoginResponseVO authenticateLogin(String username, String password) {

        // 使用ProviderManager auth方法进行验证
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
        Authentication authenticate = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        // 校验失败了
        if (Objects.isNull(authenticate)) {
            throw new AuthServiceException("用户名或密码错误");
        }
        EditorUser userDetails = (EditorUser) (authenticate.getPrincipal());

        // 4自己生成jwt token给前端
        String token = jwtUtils.generateJwtToken(authenticate);
        Date expirationDateFromToken = jwtUtils.getExpirationDateFromToken(token);

        // 5系统用户token放入redis
        redisService.setCacheObject("LOGIN_TOKEN_KEY:" + username + "::", token, CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        // 6系统用户信息放入redis
        redisService.setCacheObject("USER_INFO_KEY:" + username + "::", userDetails, CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        System.out.println(userDetails);
        //System.out.println(loginUser.getUser());

        // 7返回给前端
        return new LoginResponseVO(token, userDetails.getUsername(), expirationDateFromToken);
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
        return null;
    }
}
