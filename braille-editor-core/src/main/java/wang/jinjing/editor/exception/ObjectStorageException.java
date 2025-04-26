package wang.jinjing.editor.exception;

import wang.jinjing.common.exception.ServiceException;
import wang.jinjing.common.pojo.ErrorEnum;

public class ObjectStorageException extends ServiceException {

    public ObjectStorageException(String message, ErrorEnum errorEnum) {
        super(message, errorEnum);
    }

    public ObjectStorageException(ErrorEnum errorEnum) {
        super(errorEnum);
    }

    public ObjectStorageException(String message) {
        super(message);
    }

    public ObjectStorageException(Exception cause) {
        super(cause);
    }

    public ObjectStorageException(String message, Exception cause) {
        super(message, cause);
    }
}
