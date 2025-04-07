package wang.jinjing.editor.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import wang.jinjing.common.pojo.VO.BaseVO;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class LoginResponseVO extends BaseVO {

    private String token;

    private String username;

    private Date expireTime;

}
