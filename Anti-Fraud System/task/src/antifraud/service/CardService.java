package antifraud.service;

import antifraud.domain.TransactionValidationResult;
import antifraud.exception.CardNotFoundException;
import antifraud.model.Transaction;

import javax.transaction.Transactional;

public interface CardService {
    @Transactional
    TransactionValidationResult processAmount(Long amount, String cardNumber);

    @Transactional
    void processLimits(Transaction transaction,
                       TransactionValidationResult feedback);
}
