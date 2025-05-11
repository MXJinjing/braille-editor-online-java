package wang.jinjing.editor.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import wang.jinjing.common.pojo.DTO.BaseDTO;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO extends AbstractCaptchaDTO {

    private String email;

    private String username;

    private String password;

    private String rePassword;

}
