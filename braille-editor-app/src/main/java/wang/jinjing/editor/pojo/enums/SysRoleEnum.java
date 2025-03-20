package wang.jinjing.editor.pojo.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum SysRoleEnum {
    USER("user"),
    ADMIN("admin"),
    SUPER_ADMIN("super_admin")
    ;

    @EnumValue
    private final String value;

    SysRoleEnum(String value) {
        this.value = value;
    }
}