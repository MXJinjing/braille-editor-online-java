package wang.jinjing.editor.pojo.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ErrorEnum {

    UNKNOWN_ERROR("服务异常"),
    IO_EXCEPTION("IO异常"),
    NULL_POINTER("空指针异常"),
    ILLEGAL_STATE("状态错误"),
    ILLEGAL_ARGUMENT("参数错误"),

    // UserServiceException.java

    USER_NOT_FOUND("用户不存在"),
    USER_ID_ALREADY_EXIST("用户ID已存在"),
    USER_NAME_ALREADY_EXIST("用户名已存在"),
    CREATE_SUPER_ADMIN("不能创建超级管理员"),
    CHANGE_SUPER_ADMIN("不能修改超级管理员"),
    USER_ID_NOT_MATCH("用户ID与参数不匹配"),

    ;
    @EnumValue
    private final String message;

    ErrorEnum(String message) {
        this.message = message;
    }

}
