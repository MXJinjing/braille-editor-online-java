package wang.jinjing.common.exception;

import io.jsonwebtoken.io.IOException;
import wang.jinjing.editor.pojo.enums.ErrorEnum;

public class ServiceException extends RuntimeException{
    protected ErrorEnum errorEnum;

    public ServiceException(String message, ErrorEnum errorEnum){
        super(message);
        this.errorEnum = errorEnum;
    }

    public ServiceException(ErrorEnum errorEnum) {
        super(errorEnum.getMessage());
        this.errorEnum = errorEnum;
    }

    public ServiceException(String message) {
        super(message);
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
