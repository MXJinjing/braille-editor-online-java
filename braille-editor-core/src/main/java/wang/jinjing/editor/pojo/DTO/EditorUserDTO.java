package wang.jinjing.editor.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import wang.jinjing.common.pojo.DTO.BaseDTO;
import wang.jinjing.editor.pojo.enums.SysRoleEnum;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditorUserDTO extends BaseDTO {

    private Long id;

    private String username;

//    private String uuid;

    private String nickname;

    private SysRoleEnum sysRole;

    private String password;

    private String phone;

    private String email;

//    private Date registerAt;
//
//    private Date lastLoginAt;
//
//    private Long storageQuota;
//
//    private Boolean accountNonExpired;
//
//    private Boolean accountNonLocked;
//
//    private Boolean credentialsNonExpired;
//
//    private Boolean enabled;
}
