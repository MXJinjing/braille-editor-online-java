package wang.jinjing.editor.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import wang.jinjing.common.util.BeanConvertUtil;
import wang.jinjing.editor.pojo.DTO.LoginRequestDTO;
import wang.jinjing.editor.pojo.VO.EditorUserSimpleVO;
import wang.jinjing.editor.pojo.VO.EditorUserVO;
import wang.jinjing.editor.pojo.VO.LoginResponseVO;
import wang.jinjing.editor.pojo.entity.EditorUser;
import wang.jinjing.editor.service.secure.AuthService;
import wang.jinjing.editor.service.secure.CaptchaService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CaptchaService captchaService;

    @Value("${captcha.enabled}")
    private Boolean captchaEnabled;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO dto) {
        // 先进行验证码校验
        if(captchaEnabled) {
            //校验参数是否为空
            if(dto.getCaptchaCode() == null || dto.getCaptchaUUID() == null) {
                return ResponseEntity.badRequest().body("验证码不能为空");
            }

            if(!captchaService.verifyCaptcha(dto.getCaptchaCode(), dto.getCaptchaUUID())) {
                return ResponseEntity.badRequest().body("验证码错误");
            }
        }

        // 校验参数是否为空
        if(dto.getUsername() == null || dto.getPassword() == null) {
            return ResponseEntity.badRequest().body("用户名或密码不能为空");
        }

        LoginResponseVO loginResponseVO = authService.authenticateLogin(dto.getUsername(), dto.getPassword());
        return ResponseEntity.ok(loginResponseVO);
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
