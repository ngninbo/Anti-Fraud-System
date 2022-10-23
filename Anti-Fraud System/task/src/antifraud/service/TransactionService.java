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

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public TransactionResponse validate(Transaction transaction) throws UserNotFoundException {

        TransactionValidationResult result;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username;

        if (auth != null) {
            username = ((UserDetails) auth.getPrincipal()).getUsername();
            transaction.setUser(userService.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found!")));
        }

        final Long amount = transaction.getAmount();
        if (amount > ALLOWED.getLower() && amount <= ALLOWED.getUpper()) {
            result = ALLOWED;
        } else if (amount >= ALLOWED.getUpper() && amount <= PROHIBITED.getLower()) {
            result = MANUAL_PROCESSING;
        } else {
            result = PROHIBITED;
        }

        transaction.setStatus(result);
        transactionRepository.save(transaction);

        return TransactionResponse.builder().result(result).build();
    }
}
