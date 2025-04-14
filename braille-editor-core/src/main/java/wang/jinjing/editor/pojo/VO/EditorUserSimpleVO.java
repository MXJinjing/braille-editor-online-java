package wang.jinjing.editor.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import wang.jinjing.common.pojo.VO.BaseVO;
import wang.jinjing.editor.pojo.enums.SysRoleEnum;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditorUserSimpleVO extends BaseVO {

    private Long id;

    private String username;

    private String nickname;

    private SysRoleEnum sysRole;

    private String phone;

    private String email;

}
