package antifraud.exception;

public class AddressAlreadyExistException extends RuntimeException {

    public AddressAlreadyExistException(String message) {
        super(message);
    }
}
