package antifraud.service;

import antifraud.domain.TransactionResponse;
import antifraud.exception.UserNotFoundException;
import antifraud.model.Transaction;

public interface ITransactionService {

    TransactionResponse validate(Transaction transaction) throws UserNotFoundException;
}
