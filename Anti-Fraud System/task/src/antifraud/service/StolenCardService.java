package antifraud.service;

import antifraud.rest.CardDeletionResponse;
import antifraud.exception.CardAlreadyExistException;
import antifraud.exception.CardNotFoundException;
import antifraud.model.StolenCard;

import java.util.List;

public interface StolenCardService {

    StolenCard create(StolenCard stolenCard) throws CardAlreadyExistException;
    CardDeletionResponse removeByNumber(String number) throws CardNotFoundException;
    List<StolenCard> findAll();

    boolean isBlacklistedCardNumber(String number);
}
