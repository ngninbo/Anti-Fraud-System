package antifraud.service;

import antifraud.rest.FeedbackUpdateRequest;
import antifraud.domain.Region;
import antifraud.domain.TransactionDto;
import antifraud.rest.TransactionResponse;
import antifraud.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {

    TransactionResponse validate(TransactionDto transaction);

    List<Transaction> findAlLByNumberAndDateBetweenAndRegionNot(String number, LocalDateTime before, LocalDateTime now, Region region);

    List<Transaction> findAllByNumberAndDateIsBetweenAndIpNot(String number, LocalDateTime before, LocalDateTime now, String ip);

    Transaction updateTransactionFeedback(FeedbackUpdateRequest request);

    List<Transaction> findAll();

    List<Transaction> findAllByNumber(String number);
}
