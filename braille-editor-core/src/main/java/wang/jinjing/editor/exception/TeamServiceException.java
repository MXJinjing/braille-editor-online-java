package wang.jinjing.editor.exception;

import wang.jinjing.common.exception.ServiceException;
import wang.jinjing.common.pojo.ErrorEnum;

public class TeamServiceException extends ServiceException {

    @Override
    public ErrorEnum getErrorEnum() {
        return super.getErrorEnum();
    }

    public TeamServiceException(String message, ErrorEnum errorEnum, Throwable cause) {
        super(message, errorEnum, cause);
    }

    public TeamServiceException(ErrorEnum errorEnum, Throwable cause) {
        super(errorEnum, cause);
    }

    public TeamServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public TeamServiceException(String message, ErrorEnum errorEnum) {
        super(message, errorEnum);
    }

    public TeamServiceException(ErrorEnum errorEnum) {
        super(errorEnum);
    }

    public TeamServiceException(String message) {
        super(message);
    }

    public TeamServiceException(Exception cause) {
        super(cause);
    }
}
