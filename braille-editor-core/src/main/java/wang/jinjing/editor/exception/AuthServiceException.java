package wang.jinjing.editor.exception;


import wang.jinjing.common.exception.ServiceException;
import wang.jinjing.common.pojo.ErrorEnum;

public class AuthServiceException extends ServiceException {


    public AuthServiceException(String message, ErrorEnum errorEnum) {
        super(message, errorEnum);
    }

    public AuthServiceException(String message, ErrorEnum errorEnum, Throwable cause) {
        super(message, errorEnum, cause);
    }

    public AuthServiceException(ErrorEnum errorEnum) {
        super(errorEnum);
    }

    public AuthServiceException(ErrorEnum errorEnum, Throwable cause) {
        super(errorEnum, cause);
    }

    public AuthServiceException(String message) {
        super(message);
    }

    public AuthServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthServiceException(Exception cause) {
        super(cause);
    }
}
