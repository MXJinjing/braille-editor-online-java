package wang.jinjing.editor.pojo.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum TeamRoleEnum {
    OWNER("owner"),
    ADMIN("admin"),
    MEMBER("member");

    @EnumValue
    private final String value;

    TeamRoleEnum(String value) {
        this.value = value;
    }
}
