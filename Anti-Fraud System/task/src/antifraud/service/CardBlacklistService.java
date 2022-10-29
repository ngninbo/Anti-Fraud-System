package antifraud.service;

import antifraud.domain.CardDeletionResponse;
import antifraud.exception.CardAlreadyExistException;
import antifraud.exception.CardNotFoundException;
import antifraud.model.Card;

import java.util.List;
import java.util.Optional;

public interface CardBlacklistService {

    Card create(Card card) throws CardAlreadyExistException;
    CardDeletionResponse removeByNumber(String number) throws CardNotFoundException;
    List<Card> findAll();

    boolean isBlacklistedCardNumber(String number);
}
