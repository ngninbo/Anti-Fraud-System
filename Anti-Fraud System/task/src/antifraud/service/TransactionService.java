package antifraud.service;

import antifraud.domain.TransactionResponse;
import antifraud.domain.TransactionValidationResult;
import antifraud.exception.UserNotFoundException;
import antifraud.model.Transaction;
import antifraud.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import static antifraud.domain.TransactionValidationResult.*;

@Service
public class TransactionService implements ITransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final AddressService addressService;
    private final CardService cardService;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, UserService userService, AddressService addressService, CardService cardService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.addressService = addressService;
        this.cardService = cardService;
    }

    @Override
    @Transactional
    public TransactionResponse validate(Transaction transaction) throws UserNotFoundException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username;

        if (auth != null) {
            username = ((UserDetails) auth.getPrincipal()).getUsername();
            transaction.setUser(userService.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found!")));
        }

        var address = addressService.findByIP(transaction.getIp());
        var card = cardService.findByNUmber(transaction.getNumber());

        final Long amount = transaction.getAmount();
        String reason;
        var result = getResult(amount);

        switch (result) {
            case ALLOWED:
                if (card.isPresent() && address.isPresent()) {
                    result = PROHIBITED;
                    reason = "card-number, ip";
                } else if (card.isPresent()) {
                    result = PROHIBITED;
                    reason = "card-number";
                } else if (address.isPresent()) {
                    result = PROHIBITED;
                    reason = "ip";
                } else {
                    reason = "none";
                }

                transaction.setStatus(result);
                transactionRepository.save(transaction);
                return TransactionResponse.builder().result(result).info(reason).build();

            case PROHIBITED:
                if (card.isPresent() && address.isPresent()) {
                    reason = "amount, card-number, ip";
                }else if (card.isPresent()) {
                    reason = "amount, card-number";
                } else if (address.isPresent()) {
                    reason = "amount, ip";
                } else {
                    reason = "amount";
                }

                transaction.setStatus(result);
                transactionRepository.save(transaction);
                return TransactionResponse.builder().result(result).info(reason).build();

            case MANUAL_PROCESSING:

                if (card.isPresent() && address.isPresent()) {
                    result = PROHIBITED;
                    reason = "amount, card-number, ip";
                } else if (card.isPresent()) {
                    result = PROHIBITED;
                    reason = "card-number";
                } else if (address.isPresent()) {
                    result = PROHIBITED;
                    reason = "ip";
                } else {
                    reason = "amount";
                }

                transaction.setStatus(result);
                transactionRepository.save(transaction);
                return TransactionResponse.builder().result(result).info(reason).build();
            default:
                throw new IllegalStateException("Unexpected value: " + result);
        }
    }

    private TransactionValidationResult getResult(Long amount) {
        if (amount > ALLOWED.getLower() && amount <= ALLOWED.getUpper()) {
            return ALLOWED;
        } else if (amount >= ALLOWED.getUpper() && amount <= PROHIBITED.getLower()) {
            return MANUAL_PROCESSING;
        } else {
           return PROHIBITED;
        }
    }
}
