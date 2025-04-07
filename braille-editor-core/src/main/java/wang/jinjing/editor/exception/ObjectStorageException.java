package wang.jinjing.editor.exception;

public class ObjectStorageException extends RuntimeException {

    public ObjectStorageException() {
    }

    public ObjectStorageException(String message) {
        super(message);
    }

    public ObjectStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectStorageException(Throwable cause) {
        super(cause);
    }

    public String getErrorCode() {
        return null;
    }
}
