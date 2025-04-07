package wang.jinjing.editor.exception;

import wang.jinjing.common.exception.ServiceException;
import wang.jinjing.common.pojo.ErrorEnum;

public class CaptchaServiceException extends ServiceException {


    public CaptchaServiceException(String message, ErrorEnum errorEnum) {
        super(message, errorEnum);
    }

    public CaptchaServiceException(ErrorEnum errorEnum) {
        super(errorEnum);
    }

    public CaptchaServiceException(String message) {
        super(message);
    }

    public CaptchaServiceException(Exception cause) {
        super(cause);
    }
}
