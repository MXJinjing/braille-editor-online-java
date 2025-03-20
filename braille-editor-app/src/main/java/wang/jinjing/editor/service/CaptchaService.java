package wang.jinjing.editor.service;

import org.springframework.stereotype.Service;
import wang.jinjing.editor.pojo.VO.CaptchaResultVO;

@Service
public interface CaptchaService {
    /**
     * 获取验证码图片
     */
    public CaptchaResultVO getCaptchaImage();

    public boolean verifyCaptcha(String captchaCode, String uuid);
}
