package wang.jinjing.editor.pojo.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum SysRoleEnum {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN"),
    SUPER_ADMIN("ROLE_SUPER_ADMIN")
    ;

    @EnumValue
    private final String value;

    SysRoleEnum(String value) {
        this.value = value;
    }
}