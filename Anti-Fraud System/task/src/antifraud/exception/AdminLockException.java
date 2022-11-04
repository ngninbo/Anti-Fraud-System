package antifraud.exception;

public class AdminLockException extends RuntimeException {
    public AdminLockException(String message) {
        super(message);
    }
}
