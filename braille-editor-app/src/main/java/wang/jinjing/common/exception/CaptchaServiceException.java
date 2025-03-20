package wang.jinjing.common.exception;

import io.jsonwebtoken.io.IOException;
import wang.jinjing.editor.pojo.enums.ErrorEnum;

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
