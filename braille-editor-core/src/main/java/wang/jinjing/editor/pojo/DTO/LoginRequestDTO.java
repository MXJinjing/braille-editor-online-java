package wang.jinjing.editor.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import wang.jinjing.common.pojo.DTO.BaseDTO;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class LoginRequestDTO extends BaseDTO {

    private String username;

    private String password;

    private String captchaCode;

    private String captchaUUID;
}
