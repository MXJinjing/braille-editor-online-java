package wang.jinjing.editor.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import wang.jinjing.common.pojo.VO.BaseVO;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class CaptchaResultVO extends BaseVO {

    /**
     * 验证码图片的base64编码
     */
    private String captchaImageBase64;

    /**
     * 验证码的key
     */
    private String captchaKey;


    /**
     * 验证码的有效时间
     */

    private Date createTime;

    private Date expireTime;

}
