package wang.jinjing.common.exception;

import wang.jinjing.editor.pojo.enums.ErrorEnum;

public class UserServiceException extends ServiceException{

    public UserServiceException(String message, ErrorEnum errorEnum) {
        super(message, errorEnum);
    }

    public UserServiceException(ErrorEnum errorEnum) {
        super(errorEnum);
    }

    public UserServiceException(String message) {
        super(message);
    }

    public UserServiceException(Exception cause) {
        super(cause);
    }
}
