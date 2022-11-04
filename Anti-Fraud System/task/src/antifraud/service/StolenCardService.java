package antifraud.service;

import antifraud.rest.CardDeletionResponse;
import antifraud.model.StolenCard;

import java.util.List;

public interface StolenCardService {

    StolenCard create(StolenCard stolenCard);
    CardDeletionResponse removeByNumber(String number);
    List<StolenCard> findAll();

    boolean isBlacklistedCardNumber(String number);
}
