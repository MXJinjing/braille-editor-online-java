package wang.jinjing.common.exception;

import wang.jinjing.editor.pojo.enums.ErrorEnum;

public class AuthServiceException extends ServiceException {

    public AuthServiceException(String message, ErrorEnum errorEnum) {
        super(message, errorEnum);
    }

    public AuthServiceException(ErrorEnum errorEnum) {
        super(errorEnum);
    }

    public AuthServiceException(String message) {
        super(message);
    }

    public AuthServiceException(Exception cause) {
        super(cause);
    }
}
