package wang.jinjing.editor.service.secure;

import org.springframework.stereotype.Service;
import wang.jinjing.editor.pojo.VO.CaptchaResultVO;

@Service
public interface CaptchaService {
    /**
     * 获取验证码图片
     */
    CaptchaResultVO getCaptchaImage();

    boolean verifyCaptcha(String captchaCode, String uuid);
}
