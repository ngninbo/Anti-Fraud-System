package antifraud.domain;

import antifraud.exception.IllegalFeedbackException;

import java.util.Arrays;

public enum TransactionValidationResult {

    ALLOWED,
    MANUAL_PROCESSING,
    PROHIBITED;

    public static TransactionValidationResult parse(String feedback) throws IllegalFeedbackException {
        return Arrays.stream(TransactionValidationResult.values())
                .filter(value -> value.name().equals(feedback))
                .findFirst()
                .orElseThrow(() -> new IllegalFeedbackException("Wrong feedback!"));
    }
}
