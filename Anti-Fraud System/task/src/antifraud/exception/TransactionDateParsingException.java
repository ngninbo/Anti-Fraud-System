package antifraud.exception;

public class TransactionDateParsingException extends RuntimeException {
    public TransactionDateParsingException(String message) {
        super(message);
    }
}
