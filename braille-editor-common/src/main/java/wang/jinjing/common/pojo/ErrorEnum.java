package wang.jinjing.common.pojo;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorEnum {

    UNKNOWN_ERROR("服务异常",HttpStatus.INTERNAL_SERVER_ERROR),
    IO_EXCEPTION("IO异常",HttpStatus.INTERNAL_SERVER_ERROR),
    NULL_POINTER("空指针异常",HttpStatus.INTERNAL_SERVER_ERROR),
    ILLEGAL_STATE("状态错误",HttpStatus.INTERNAL_SERVER_ERROR),
    ILLEGAL_ARGUMENT("参数错误",HttpStatus.BAD_REQUEST),

    UPDATE_FAIL("更新失败",HttpStatus.INTERNAL_SERVER_ERROR),
    DELETE_FAIL("删除失败",HttpStatus.INTERNAL_SERVER_ERROR),
    CREATE_FAIL("创建失败",HttpStatus.INTERNAL_SERVER_ERROR),
    UPDATE_ID_NOT_MATCH("ID与参数不匹配",HttpStatus.INTERNAL_SERVER_ERROR),

    EMAIL_CODE_EXPIRED("邮箱验证码已过期",HttpStatus.BAD_REQUEST),
    EMAIL_CODE_ERROR("邮箱验证码错误",HttpStatus.BAD_REQUEST),
    UPDATE_ENTITY_ID_NOT_MATCH("ID与参数不匹配",HttpStatus.BAD_REQUEST),
    // passwords

    PASSWORD_TOO_SHORT("密码太短",HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_LONG("密码太长",HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_WEAK("密码太弱",HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_SUPPORTED("密码包含不支持的字符",HttpStatus.BAD_REQUEST),

    // UserServiceException.java
    USER_NOT_FOUND("用户不存在",HttpStatus.BAD_REQUEST),
    USER_ID_ALREADY_EXIST("用户ID已存在",HttpStatus.CONFLICT),
    USER_NAME_ALREADY_EXIST("用户名已存在",HttpStatus.CONFLICT),
    CREATE_SUPER_ADMIN("不能创建超级管理员",HttpStatus.NOT_ACCEPTABLE),
    CHANGE_SUPER_ADMIN("不能修改超级管理员",HttpStatus.NOT_ACCEPTABLE),
    USER_ACCOUNT_DISABLED("用户账号已经被禁用",HttpStatus.CONFLICT),
    BUCKET_CREATE_FAIL("桶创建失败",HttpStatus.INTERNAL_SERVER_ERROR),
    BUCKET_DELETE_FAIL("桶删除失败",HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_USER_ID("无法修改其他用户的信息，检查参数",HttpStatus.BAD_REQUEST),

    // TeamServiceException.java
    TEAM_OWNER_NOT_FOUND("所有者用户不存在",HttpStatus.NOT_FOUND),
    TEAM_OWNER_ACCOUNT_DISABLED("所有者用户已被禁用",HttpStatus.CONFLICT),
    TEAM_NOT_FOUND("团队不存在",HttpStatus.NOT_FOUND),
    TEAM_CANNOT_CHANGE_OWNER_IN_UPDATE("不支持在更新时修改团队所有者",HttpStatus.NOT_ACCEPTABLE),
    TEAM_CANNOT_REMOVE_OWNER("不支持直接移除团队所有者",HttpStatus.NOT_ACCEPTABLE),
    USER_NOT_IN_TEAM("用户不在目标团队中",HttpStatus.NOT_FOUND),
    USER_ALREADY_IN_TEAM("用户已在目标团队中",HttpStatus.CONFLICT),


    FILE_METADATA_NOT_FOUND("文件信息未找到",HttpStatus.NOT_FOUND),
    FILE_MOVE_FAIL("文件移动失败",HttpStatus.INTERNAL_SERVER_ERROR),

    RECYCLE_RECORD_NOT_FOUND("回收站记录未被找到",HttpStatus.NOT_FOUND),
    ORIGIN_PATH_OCCUPIED("原来的路径被占用了",HttpStatus.CONFLICT),
    FILE_UPDATE_FAILED("文件更新失败",HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_COPY_FAIL("文件更新失败",HttpStatus.INTERNAL_SERVER_ERROR),
    SUB_COPY_FAIL("子项移动更新失败",HttpStatus.INTERNAL_SERVER_ERROR),
    USERNAME_OR_PASSWORD_ERROR("用户名或密码错误", HttpStatus.UNAUTHORIZED),
    USER_BUCKET_ALREADY_INIT("用户存储桶已经初始化",HttpStatus.NO_CONTENT ),
    USER_BUCKET_CREATE_FAIL("用户存储桶创建失败",HttpStatus.INTERNAL_SERVER_ERROR),
    USER_BUCKET_NOT_INIT("用户存储桶未初始化", HttpStatus.NOT_ACCEPTABLE ),
    UPLOAD_BYTES_FAILED("上传字节错误", HttpStatus.INTERNAL_SERVER_ERROR ),
    UPLOAD_STREAM_FAILED("上传文件流错误", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DOWNLOAD_FAILED("文件下载失败", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED("文件删除失败", HttpStatus.INTERNAL_SERVER_ERROR ),

    OSS_GET_METADATA_FAILED("无法获取文件元信息", HttpStatus.INTERNAL_SERVER_ERROR ),
    BUCKET_NOT_EXIST("指定的桶不存在",HttpStatus.NOT_FOUND ),
    FILE_NAME_ALREADY_EXIST("新的文件名已经存在", HttpStatus.CONFLICT),
    PATH_NOT_EXISTS("指定的路径不存在", HttpStatus.NOT_FOUND),
    FILE_CONTENT_NOT_FOUND("文件内容已丢失", HttpStatus.INTERNAL_SERVER_ERROR ),
    RENAME_ROOT_NOT_ALLOWED("不允许重命名根目录",HttpStatus.NOT_ACCEPTABLE ),
    DEST_FILE_TYPE_CONFLICT("目标路径已存在同名不同类型（目录、文件）文件无法覆盖", HttpStatus.CONFLICT ),
    ;

    private final String message;

    private final HttpStatus status;

    ErrorEnum(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

}
