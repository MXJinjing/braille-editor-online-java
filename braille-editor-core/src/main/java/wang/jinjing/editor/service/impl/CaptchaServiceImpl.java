package wang.jinjing.editor.service.impl;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.util.StrUtil;
import com.google.code.kaptcha.Producer;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FastByteArrayOutputStream;
import wang.jinjing.editor.exception.CaptchaServiceException;
import wang.jinjing.editor.pojo.VO.CaptchaResultVO;
import wang.jinjing.editor.service.secure.CaptchaService;
import wang.jinjing.editor.service.RedisService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CaptchaServiceImpl implements CaptchaService {

    @Qualifier("captchaProducer")
    @Autowired
    private Producer captchaProducer;

    @Autowired
    private RedisService redisService;

    @Value("${captcha.enabled}")
    private static final boolean isCaptchaEnabled = true;

    @Value("${captcha.key.prefix}")
    private static final String CAPTCHA_KEY_PREFIX = "captcha";

    @Value("${captcha.expiration.secs}")
    private static final long CAPTCHA_EXPIRATION = 300;

    @Override
    public CaptchaResultVO getCaptchaImage() {
        // 获取新的UUID
        String string = UUID.randomUUID().toString();
        String verifyKey = CAPTCHA_KEY_PREFIX + "::" + string;

        String text = captchaProducer.createText();

        BufferedImage image = captchaProducer.createImage(text);

        // 在redis中存储验证码的键
        redisService.set(verifyKey, text, CAPTCHA_EXPIRATION, TimeUnit.SECONDS);


        // 转换流信息写出
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try
        {
            ImageIO.write(image, "jpg", os);
        }
        catch (IOException e)
        {
            throw new CaptchaServiceException(e);
        }
        // 将图片转化为字节流，BASE64编码
        byte[] bytes = os.toByteArray();

        // 获取过期时间和创建时间
        Date expireTime = new Date(System.currentTimeMillis() + CAPTCHA_EXPIRATION * 1000);
        Date createTime = new Date();

        return new CaptchaResultVO(
                "data:image/gif;base64," + Base64Encoder.encode(bytes),
                string,
                createTime,
                expireTime
        );

    }

    @Override
    public boolean verifyCaptcha(@Valid String captchaCode, String uuid) {
        String verifyRedisKey = CAPTCHA_KEY_PREFIX + "::" + uuid;

        // 将redis的验证码与用户输入值进行匹配
        String s = redisService.get(verifyRedisKey);

        redisService.del(verifyRedisKey);

        // 如果验证码为空，返回false
        if (StrUtil.isBlank(s)) {
            return false;
        }
        // 如果验证码不匹配，返回false
        return StrUtil.equals(captchaCode, s);
    }
}
