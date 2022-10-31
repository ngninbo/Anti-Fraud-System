package antifraud.service;

import antifraud.model.StolenCard;
import antifraud.rest.CardDeletionResponse;
import antifraud.exception.CardAlreadyExistException;
import antifraud.exception.CardNotFoundException;
import antifraud.repository.StolenCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class StolenCardServiceImpl implements StolenCardService {

    private final StolenCardRepository stolenCardRepository;

    @Autowired
    public StolenCardServiceImpl(StolenCardRepository stolenCardRepository) {
        this.stolenCardRepository = stolenCardRepository;
    }

    @Override
    @Transactional
    public StolenCard create(StolenCard newStolenCard) throws CardAlreadyExistException {

        var car = stolenCardRepository.findByNumber(newStolenCard.getNumber());

        if (car.isPresent()) {
            throw new CardAlreadyExistException("Card already exist!");
        }

        return stolenCardRepository.save(newStolenCard);
    }

    @Override
    @Transactional
    public CardDeletionResponse removeByNumber(String number) throws CardNotFoundException {
        StolenCard stolenCard = stolenCardRepository.findByNumber(number).orElseThrow(() -> new CardNotFoundException("Card not found!"));
        stolenCardRepository.delete(stolenCard);

        String status = String.format("Card %s successfully removed!", number);

        return CardDeletionResponse.builder().status(status).build();
    }

    @Override
    public List<StolenCard> findAll() {
        return stolenCardRepository.findAll();
    }

    @Override
    public boolean isBlacklistedCardNumber(String number) {
        return this.stolenCardRepository.findByNumber(number).isPresent();
    }
}
