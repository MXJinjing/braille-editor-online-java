package wang.jinjing.editor.pojo.DTO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import wang.jinjing.common.pojo.DTO.BaseDTO;
import wang.jinjing.editor.pojo.enums.SysRoleEnum;

import java.util.Date;

public class EditorUserDTO extends BaseDTO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String uuid;

    private String nickname;

    private SysRoleEnum sysRole;

    private String password;

    private String phone;

    private String email;

    private Date registerAt;

    private Date lastLoginAt;

    private Long storageQuota;

    private Boolean accountNonExpired;

    private Boolean accountNonLocked;

    private Boolean credentialsNonExpired;

    private Boolean enabled;
}
