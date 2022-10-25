package antifraud.service;


import antifraud.domain.CardDeletionResponse;
import antifraud.exception.CardAlreadyExistException;
import antifraud.exception.CardNotFoundException;
import antifraud.model.Card;
import antifraud.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;

    @Autowired
    public CardServiceImpl(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Override
    @Transactional
    public Card create(Card newCard) throws CardAlreadyExistException {

        var car = cardRepository.findByNumber(newCard.getNumber());

        if (car.isPresent()) {
            throw new CardAlreadyExistException("Card already exist!");
        }

        return cardRepository.save(newCard);
    }

    @Override
    @Transactional
    public CardDeletionResponse removeByNumber(String number) throws CardNotFoundException {
        Card card = cardRepository.findByNumber(number).orElseThrow(() -> new CardNotFoundException("Card not found!"));
        cardRepository.delete(card);

        String status = String.format("Card %s successfully removed!", number);

        return CardDeletionResponse.builder().status(status).build();
    }

    @Override
    public List<Card> findAll() {
        return cardRepository.findAll();
    }

    @Override
    public Optional<Card> findByNUmber(String number) {
        return cardRepository.findByNumber(number);
    }
}
