package wang.jinjing.editor.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wang.jinjing.editor.pojo.DTO.LoginRequestDTO;
import wang.jinjing.editor.pojo.VO.LoginResponseVO;
import wang.jinjing.editor.service.AuthService;
import wang.jinjing.editor.service.CaptchaService;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CaptchaService captchaService;

    @Value("${captcha.enabled}")
    private final Boolean captchaEnabled = true;


    @PostMapping("/login")
    public LoginResponseVO login(LoginRequestDTO dto) {
        // 先进行验证码校验
        if(captchaEnabled) {
            //校验参数是否为空
            if(dto.getCaptchaCode() == null || dto.getCaptchaUUID() == null) {
                throw new AuthenticationServiceException("验证码不能为空");
            }

            if(!captchaService.verifyCaptcha(dto.getCaptchaCode(), dto.getCaptchaUUID())) {
                throw new AuthenticationServiceException("验证码错误");
            }
        }

        // 校验参数是否为空
        if(dto.getUsername() == null || dto.getPassword() == null) {
            throw new AuthenticationServiceException("用户名或密码不能为空");
        }

        return authService.authenticateLogin(dto.getUsername(), dto.getPassword());
    }
}
