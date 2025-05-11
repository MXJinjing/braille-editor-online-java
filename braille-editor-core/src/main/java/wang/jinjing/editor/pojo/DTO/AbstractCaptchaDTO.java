package wang.jinjing.editor.pojo.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractCaptchaDTO {

    protected String captchaCode;

    protected String captchaUUID;
}
