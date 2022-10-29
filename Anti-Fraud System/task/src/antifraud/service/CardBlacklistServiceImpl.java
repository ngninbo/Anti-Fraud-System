package antifraud.service;

import antifraud.domain.CardDeletionResponse;
import antifraud.exception.CardAlreadyExistException;
import antifraud.exception.CardNotFoundException;
import antifraud.model.Card;
import antifraud.repository.CardBlackListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class CardBlacklistServiceImpl implements CardBlacklistService {

    private final CardBlackListRepository cardBlackListRepository;

    @Autowired
    public CardBlacklistServiceImpl(CardBlackListRepository cardBlackListRepository) {
        this.cardBlackListRepository = cardBlackListRepository;
    }

    @Override
    @Transactional
    public Card create(Card newCard) throws CardAlreadyExistException {

        var car = cardBlackListRepository.findByNumber(newCard.getNumber());

        if (car.isPresent()) {
            throw new CardAlreadyExistException("Card already exist!");
        }

        return cardBlackListRepository.save(newCard);
    }

    @Override
    @Transactional
    public CardDeletionResponse removeByNumber(String number) throws CardNotFoundException {
        Card card = cardBlackListRepository.findByNumber(number).orElseThrow(() -> new CardNotFoundException("Card not found!"));
        cardBlackListRepository.delete(card);

        String status = String.format("Card %s successfully removed!", number);

        return CardDeletionResponse.builder().status(status).build();
    }

    @Override
    public List<Card> findAll() {
        return cardBlackListRepository.findAll();
    }

    @Override
    public boolean isBlacklistedCardNumber(String number) {
        return this.cardBlackListRepository.findByNumber(number).isPresent();
    }
}
