package antifraud.handler;

import antifraud.domain.AntiFraudCustomErrorMessage;
import antifraud.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@ControllerAdvice
public class AntiFraudExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<AntiFraudCustomErrorMessage> handleValidationError(MethodArgumentNotValidException e,
                                                                             HttpServletRequest request) {
        AntiFraudCustomErrorMessage body = AntiFraudCustomErrorMessage.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(e.getBindingResult().getAllErrors().get(0).getDefaultMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({UserAlreadyExistException.class, AddressAlreadyExistException.class, CardAlreadyExistException.class})
    public ResponseEntity<AntiFraudCustomErrorMessage> handleConflict(Exception exception, HttpServletRequest request) {
        AntiFraudCustomErrorMessage body = AntiFraudCustomErrorMessage.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(exception.getMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({UserNotFoundException.class, AddressNotFoundException.class, CardNotFoundException.class})
    public ResponseEntity<AntiFraudCustomErrorMessage> handleNotFound(Exception exception, HttpServletRequest httpServletRequest) {
        AntiFraudCustomErrorMessage body = AntiFraudCustomErrorMessage.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(exception.getMessage())
                .path(httpServletRequest.getRequestURI())
                .build();
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({RoleUpdateException.class, AdminLockException.class, InvalidRegionException.class, TransactionDateParsingException.class})
    public ResponseEntity<AntiFraudCustomErrorMessage> handleRoleException(Exception e, HttpServletRequest request) {
        AntiFraudCustomErrorMessage body = AntiFraudCustomErrorMessage.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(e.getLocalizedMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
