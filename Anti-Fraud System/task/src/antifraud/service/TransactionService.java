package antifraud.service;

import antifraud.domain.Region;
import antifraud.domain.TransactionDto;
import antifraud.domain.TransactionResponse;
import antifraud.exception.InvalidRegionException;
import antifraud.exception.TransactionDateParsingException;
import antifraud.exception.UserNotFoundException;
import antifraud.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {

    TransactionResponse validate(TransactionDto transaction) throws UserNotFoundException, InvalidRegionException, TransactionDateParsingException;

    List<Transaction> findAlLByNumberAndDateBetweenAndRegionNot(String number, LocalDateTime before, LocalDateTime now, Region region);

    List<Transaction> findAllByNumberAndDateIsBetweenAndIpNot(String number, LocalDateTime before, LocalDateTime now, String ip);
}
