package antifraud.domain;

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

    public int getLower() {
        return lower;
    }

    public int getUpper() {
        return upper;
    }
}
