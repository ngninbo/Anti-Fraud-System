package antifraud.service;

import antifraud.domain.TransactionValidationResult;
import antifraud.exception.CardNotFoundException;
import antifraud.model.Card;
import antifraud.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import static antifraud.domain.TransactionValidationResult.*;

@Service
public class CardServiceImpl implements CardService {

    private final CardRepository repository;

    @Autowired
    public CardServiceImpl(CardRepository cardRepository) {
        this.repository = cardRepository;
    }

    @Override
    @Transactional
    public TransactionValidationResult processAmount(Long amount, String cardNumber) {
        var cardFromRepo = repository.findByNumber(cardNumber);
        Card card;

        if (cardFromRepo.isEmpty()) {
            card = new Card(cardNumber);
            repository.save(card);
        } else {
            card = cardFromRepo.get();
        }

        Long maxAllowed = card.getMaxAllowed();
        Long maxManual = card.getMaxManual();

        if (amount <= maxAllowed) {
            return ALLOWED;
        } else if (amount <= maxManual) {
            return MANUAL_PROCESSING;
        } else {
            return PROHIBITED;
        }
    }

    @Override
    @Transactional
    public void processLimits(String cardNumber, Long transactionAmount,
                              TransactionValidationResult result, TransactionValidationResult feedback) throws CardNotFoundException {

        Card card = repository.findByNumber(cardNumber).orElseThrow(() -> new CardNotFoundException(String.format("Card by number %s not found", cardNumber)));

        switch (feedback) {
            case ALLOWED:
                switch (result) {
                    case MANUAL_PROCESSING:
                        card.setMaxAllowed(increaseLimit(card.getMaxAllowed(), transactionAmount));
                        break;
                    case PROHIBITED:
                        card.setMaxManual(increaseLimit(card.getMaxManual(), transactionAmount));
                        card.setMaxAllowed(increaseLimit(card.getMaxAllowed(), transactionAmount));
                        break;
                }
                break;
            case MANUAL_PROCESSING:
                switch (result) {
                    case ALLOWED:
                        card.setMaxAllowed(decreaseLimit(card.getMaxAllowed(), transactionAmount));
                        break;
                    case PROHIBITED:
                        card.setMaxManual(increaseLimit(card.getMaxManual(), transactionAmount));
                        break;
                }
                break;
            case PROHIBITED:
                switch (result) {
                    case ALLOWED:
                        card.setMaxAllowed(decreaseLimit(card.getMaxAllowed(), transactionAmount));
                        card.setMaxManual(decreaseLimit(card.getMaxManual(), transactionAmount));
                        break;
                    case MANUAL_PROCESSING:
                        card.setMaxManual(decreaseLimit(card.getMaxManual(), transactionAmount));
                        break;
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + feedback);
        }

        repository.save(card);
    }

    private long increaseLimit(long currentLimit, long amount) {
        return (long) Math.ceil(0.8 * currentLimit + 0.2 * amount);
    }

    private long decreaseLimit(long currentLimit, long amount) {
        return (long) Math.ceil(0.8 * currentLimit - 0.2 * amount);
    }
}
