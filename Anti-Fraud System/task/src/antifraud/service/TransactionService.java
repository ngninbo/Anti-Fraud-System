package antifraud.service;

import antifraud.domain.TransactionResponse;
import antifraud.domain.TransactionValidationResult;
import antifraud.model.Transaction;
import org.springframework.stereotype.Service;

import static antifraud.domain.TransactionValidationResult.*;

@Service
public class TransactionService implements ITransactionService {

    @Override
    public TransactionResponse validate(Transaction transaction) {

        TransactionValidationResult result;

        final Long amount = transaction.getAmount();
        if (amount > ALLOWED.getLower() && amount <= ALLOWED.getUpper()) {
            result = ALLOWED;
        } else if (amount >= ALLOWED.getUpper() && amount <= PROHIBITED.getLower()) {
            result = MANUAL_PROCESSING;
        } else {
            result = PROHIBITED;
        }

        return TransactionResponse.builder().result(result).build();
    }
}
