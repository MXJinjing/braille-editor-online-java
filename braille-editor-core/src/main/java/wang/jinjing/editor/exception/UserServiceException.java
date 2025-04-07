package wang.jinjing.editor.exception;

import wang.jinjing.common.exception.ServiceException;
import wang.jinjing.common.pojo.ErrorEnum;

public class UserServiceException extends ServiceException {


    public UserServiceException(String message, ErrorEnum errorEnum) {
        super(message, errorEnum);
    }

    public UserServiceException(String message, ErrorEnum errorEnum, Throwable cause) {
        super(message, errorEnum, cause);
    }

    public UserServiceException(ErrorEnum errorEnum) {
        super(errorEnum);
    }

    public UserServiceException(ErrorEnum errorEnum, Throwable cause) {
        super(errorEnum, cause);
    }

    public UserServiceException(String message) {
        super(message);
    }

    public UserServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserServiceException(Exception cause) {
        super(cause);
    }
}
