package wang.jinjing.editor.controller.publics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wang.jinjing.editor.pojo.VO.CaptchaResultVO;
import wang.jinjing.editor.service.secure.CaptchaService;

@RestController
@RequestMapping("/api/public/captcha")
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    @GetMapping("/image")
    public ResponseEntity<CaptchaResultVO> getCaptchaImage() {
        CaptchaResultVO captchaImage = captchaService.getCaptchaImage();
        return ResponseEntity.ok(captchaImage);
    }

    @GetMapping("/verify")
    public ResponseEntity<Boolean> verifyCaptcha(String captchaCode, String uuid) {
        boolean result = captchaService.verifyCaptcha(captchaCode, uuid);
        return ResponseEntity.ok(result);
    }
}
