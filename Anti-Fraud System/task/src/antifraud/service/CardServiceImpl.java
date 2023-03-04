package antifraud.service;

import antifraud.domain.TransactionValidationResult;
import antifraud.exception.CardNotFoundException;
import antifraud.model.Card;
import antifraud.model.Transaction;
import antifraud.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import static antifraud.domain.TransactionValidationResult.*;
import static antifraud.util.AntiFraudUtil.decreaseLimit;
import static antifraud.util.AntiFraudUtil.increaseLimit;

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

        Card card = findCardByNumberAndSaveIfAbsent(cardNumber);

        if (amount <= card.getMaxAllowed()) {
            return ALLOWED;
        } else if (amount <= card.getMaxManual()) {
            return MANUAL_PROCESSING;
        } else {
            return PROHIBITED;
        }
    }

    @Override
    @Transactional
    public void processLimits(Transaction transaction,
                              TransactionValidationResult feedback) {

        long transactionAmount = transaction.getAmount();
        TransactionValidationResult result = transaction.getResult();
        final String cardNumber = transaction.getNumber();

        Card card = repository.findByNumber(cardNumber)
                .orElseThrow(() -> new CardNotFoundException(String.format("Card by number %s not found", cardNumber)));

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

    private Card findCardByNumberAndSaveIfAbsent(String cardNumber) {
        var cardFromRepo = repository.findByNumber(cardNumber);

        return cardFromRepo.isEmpty() ? save(cardNumber) : cardFromRepo.get();
    }

    private Card save(String cardNumber) {
        Card card = new Card(cardNumber);
        repository.save(card);
        return card;
    }
}
