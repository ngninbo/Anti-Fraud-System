package antifraud.service;

import antifraud.domain.TransactionValidationResult;
import antifraud.exception.CardNotFoundException;

import javax.transaction.Transactional;

public interface CardService {
    @Transactional
    TransactionValidationResult processAmount(Long amount, String cardNumber);

    @Transactional
    void processLimits(String cardNumber, Long transactionAmount, TransactionValidationResult result, TransactionValidationResult feedback) throws CardNotFoundException;
}
