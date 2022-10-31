package antifraud.service;

import antifraud.domain.TransactionValidationResult;
import antifraud.model.Card;
import antifraud.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

import static antifraud.domain.TransactionValidationResult.*;

@Service
public class CardServiceImpl implements CardService {

    private static final long MAX_ALLOWED = 200L;
    private static final long MAX_MANUAL = 1500L;
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
            card = new Card(cardNumber, MAX_ALLOWED, MAX_MANUAL);
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
                              TransactionValidationResult result, TransactionValidationResult feedback) {
        Optional<Card> optionalCard = repository.findByNumber(cardNumber);

        if (optionalCard.isPresent()) {
            Card card = optionalCard.get();
            switch (feedback) {
                case ALLOWED:
                    if (result.equals(MANUAL_PROCESSING)) {
                        card.setMaxAllowed(increaseLimit(card.getMaxAllowed(), transactionAmount));
                    } else if (result.equals(PROHIBITED)) {
                        card.setMaxManual(increaseLimit(card.getMaxManual(), transactionAmount));
                        card.setMaxAllowed(increaseLimit(card.getMaxAllowed(), transactionAmount));
                    }
                    break;
                case MANUAL_PROCESSING:
                    if (result.equals(ALLOWED)) {
                        card.setMaxAllowed(decreaseLimit(card.getMaxAllowed(), transactionAmount));
                    } else if (result.equals(PROHIBITED)) {
                        card.setMaxManual(increaseLimit(card.getMaxManual(), transactionAmount));
                    }
                    break;
                case PROHIBITED:
                    if (result.equals(ALLOWED)) {
                        card.setMaxAllowed(decreaseLimit(card.getMaxAllowed(), transactionAmount));
                        card.setMaxManual(decreaseLimit(card.getMaxManual(), transactionAmount));
                    } else if (result.equals(MANUAL_PROCESSING)) {
                        card.setMaxManual(decreaseLimit(card.getMaxManual(), transactionAmount));
                    }
                    break;
            }

            repository.save(card);
        }
    }

    private long increaseLimit(long currentLimit, long amount) {
        return (long) Math.ceil(0.8 * currentLimit + 0.2 * amount);
    }

    private long decreaseLimit(long currentLimit, long amount) {
        return (long) Math.ceil(0.8 * currentLimit - 0.2 * amount);
    }
}
