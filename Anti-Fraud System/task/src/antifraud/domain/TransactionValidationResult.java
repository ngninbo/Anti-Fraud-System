package antifraud.domain;

import antifraud.exception.IllegalFeedbackException;

public enum TransactionValidationResult {

    ALLOWED(0, 200),
    MANUAL_PROCESSING(200, 1500),
    PROHIBITED(1500, Integer.MAX_VALUE);

    private final int lower;
    private final int upper;

    TransactionValidationResult(int lower, int upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public static TransactionValidationResult parse(String feedback) throws IllegalFeedbackException {
        switch (feedback) {
            case "ALLOWED":
                return ALLOWED;
            case "MANUAL_PROCESSING":
                return MANUAL_PROCESSING;
            case "PROHIBITED":
                return PROHIBITED;
            default:
                throw new IllegalFeedbackException("Wrong feedback!");
        }
    }

    public int getLower() {
        return lower;
    }

    public int getUpper() {
        return upper;
    }
}
