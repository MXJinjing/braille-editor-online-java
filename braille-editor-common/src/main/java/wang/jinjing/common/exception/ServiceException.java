package wang.jinjing.common.exception;

import io.jsonwebtoken.io.IOException;
import lombok.Getter;
import wang.jinjing.common.pojo.ErrorEnum;

@Getter
public class ServiceException extends RuntimeException{
    public ErrorEnum errorEnum;

    public ServiceException(String message, ErrorEnum errorEnum){
        super(message);
        this.errorEnum = errorEnum;
    }

    public ServiceException(String message, ErrorEnum errorEnum, Throwable cause){
        super(message,cause);
        this.errorEnum = errorEnum;
    }

    public ServiceException(ErrorEnum errorEnum) {
        super("");
        this.errorEnum = errorEnum;
    }

    public ServiceException(ErrorEnum errorEnum, Throwable cause) {
        super("", cause);
        this.errorEnum = errorEnum;
    }

    public ServiceException(String message) {
        super(message);
        this.errorEnum = ErrorEnum.UNKNOWN_ERROR;
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorEnum = ErrorEnum.UNKNOWN_ERROR;
    }

    public ServiceException(Exception cause) {
        if(cause instanceof IllegalArgumentException){
            this.errorEnum = ErrorEnum.ILLEGAL_ARGUMENT;
        } else if (cause instanceof IllegalStateException){
            this.errorEnum = ErrorEnum.ILLEGAL_STATE;
        } else if (cause instanceof NullPointerException){
            this.errorEnum = ErrorEnum.NULL_POINTER;
        } else if (cause instanceof IOException){
            this.errorEnum = ErrorEnum.IO_EXCEPTION;
        } else {
            this.errorEnum = ErrorEnum.UNKNOWN_ERROR;
        }
    }
}
