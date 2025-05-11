package wang.jinjing.editor.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import wang.jinjing.common.exception.ServiceException;
import wang.jinjing.common.pojo.DTO.BaseDTO;
import wang.jinjing.common.pojo.ErrorEnum;
import wang.jinjing.common.service.AbstractCRUDService;
import wang.jinjing.common.util.BeanConvertUtil;
import wang.jinjing.editor.pojo.DTO.AbstractCaptchaDTO;
import wang.jinjing.editor.pojo.DTO.LoginRequestDTO;
import wang.jinjing.editor.pojo.DTO.RegisterRequestDTO;
import wang.jinjing.editor.pojo.VO.EditorUserSimpleVO;
import wang.jinjing.editor.pojo.VO.EditorUserVO;
import wang.jinjing.editor.pojo.VO.LoginResponseVO;
import wang.jinjing.editor.pojo.entity.EditorUser;
import wang.jinjing.editor.repository.EditorUserRepository;
import wang.jinjing.editor.service.secure.AuthService;
import wang.jinjing.editor.service.secure.CaptchaService;

import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private CaptchaService captchaService;

    @Value("${captcha.enabled}")
    private Boolean captchaEnabled;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO dto) {
        // 先进行验证码校验
        ResponseEntity<String> checkCaptcha = checkCaptcha(dto);
        if (checkCaptcha != null) return checkCaptcha;

        // 校验参数是否为空
        if(dto.getUsername() == null || dto.getPassword() == null) {
            return ResponseEntity.badRequest().body("用户名或密码不能为空");
        }

        LoginResponseVO loginResponseVO = authService.authenticateLogin(dto.getUsername(), dto.getPassword());
        return ResponseEntity.ok(loginResponseVO);
    }

    @Nullable
    private ResponseEntity<String> checkCaptcha(AbstractCaptchaDTO dto) {
        if(captchaEnabled) {
            //校验参数是否为空
            if(dto.getCaptchaCode() == null || dto.getCaptchaUUID() == null) {
                return ResponseEntity.badRequest().body("验证码不能为空");
            }

            if(!captchaService.verifyCaptcha(dto.getCaptchaCode(), dto.getCaptchaUUID())) {
                return ResponseEntity.badRequest().body("验证码错误");
            }
        }
        return null;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO dto) {
        // 先进行验证码验证
        ResponseEntity<String> checkCaptcha = checkCaptcha(dto);
        if (checkCaptcha != null) return checkCaptcha;

        // 校验参数是否为空
        if(dto.getUsername() == null || dto.getEmail() == null || dto.getPassword() == null) {
            return ResponseEntity.badRequest().build();
        }

        if(!dto.getPassword().equals(dto.getRePassword())){
            return ResponseEntity.badRequest().body("两次输入的密码不一致");
        }

        EditorUserVO vo =  authService.registerUser(dto.getUsername(),dto.getEmail(),dto.getPassword());

        return ResponseEntity.ok(vo);
    }



    @GetMapping("/check")
    public ResponseEntity<?> checkLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return ResponseEntity.ok("Authorized");
    }



}
