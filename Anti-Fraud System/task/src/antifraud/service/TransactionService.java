package antifraud.service;

import antifraud.rest.FeedbackUpdateRequest;
import antifraud.domain.Region;
import antifraud.domain.TransactionDto;
import antifraud.rest.TransactionResponse;
import antifraud.exception.*;
import antifraud.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {

    TransactionResponse validate(TransactionDto transaction) throws UserNotFoundException, InvalidRegionException, TransactionDateParsingException;

    List<Transaction> findAlLByNumberAndDateBetweenAndRegionNot(String number, LocalDateTime before, LocalDateTime now, Region region);

    List<Transaction> findAllByNumberAndDateIsBetweenAndIpNot(String number, LocalDateTime before, LocalDateTime now, String ip);

    Transaction updateTransactionFeedback(FeedbackUpdateRequest request) throws TransactionNotFoundException, TransactionFeedbackAlreadyExistException, IllegalFeedbackException, TransactionFeedbackUpdateException, CardNotFoundException;

    List<Transaction> findAll();

    List<Transaction> findAllByNumber(String number) throws InvalidNumberException, TransactionNotFoundException;
}
