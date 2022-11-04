package antifraud.exception;

public class TransactionFeedbackAlreadyExistException extends RuntimeException {
    public TransactionFeedbackAlreadyExistException(String message) {
        super(message);
    }
}
