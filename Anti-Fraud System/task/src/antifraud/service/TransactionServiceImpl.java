package antifraud.service;

import antifraud.domain.*;
import antifraud.exception.*;
import antifraud.model.Transaction;
import antifraud.repository.TransactionRepository;
import antifraud.rest.FeedbackUpdateRequest;
import antifraud.rest.TransactionResponse;
import antifraud.util.AntiFraudUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;

import static antifraud.domain.TransactionValidationResult.*;

@Service
public class TransactionServiceImpl implements TransactionService {

    Long MAX_ALLOWED = 200L;
    Long MAX_MANUAL_PROCESSING = 1500L;

    private final TransactionRepository transactionRepository;
    private final UserServiceImpl userService;
    private final SuspiciousIpService suspiciousIpService;
    private final StolenCardService stolenCardService;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, UserServiceImpl userService,
                                  SuspiciousIpService suspiciousIpService, StolenCardService stolenCardService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.suspiciousIpService = suspiciousIpService;
        this.stolenCardService = stolenCardService;
    }

    @Override
    @Transactional
    public TransactionResponse validate(TransactionDto dto) throws UserNotFoundException, InvalidRegionException, DateTimeException, TransactionDateParsingException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username;

        final Region region = Region.toRegion(dto.getRegion());
        final LocalDateTime date;
        try {
            date = LocalDateTime.parse(dto.getDate());
        } catch (Exception e) {
            throw new TransactionDateParsingException("Invalid date format");
        }
        Transaction transaction = new Transaction(dto.getAmount(), dto.getIp(), dto.getNumber(), region, date);

        if (auth != null) {
            username = ((UserDetails) auth.getPrincipal()).getUsername();
            transaction.setUser(userService.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found!")));
        }

        boolean isBlackListedAddress = suspiciousIpService.isBlacklistedIp(transaction.getIp());
        boolean isBlackListedCardNumber = stolenCardService.isBlacklistedCardNumber(transaction.getNumber());
        LocalDateTime lastHour = date.minusHours(1);
        var  numTransactionsNotInRegion = this.findAlLByNumberAndDateBetweenAndRegionNot(transaction.getNumber(), lastHour, date, region).stream().map(Transaction::getRegion).distinct().count();
        var numTransactionsNotWithIp = this.findAllByNumberAndDateIsBetweenAndIpNot(transaction.getNumber(), lastHour, date, transaction.getIp()).stream().map(Transaction::getIp).distinct().count();
        
        var result = getResult(transaction.getAmount());
        var tb = TransactionResponse.builder();

        final boolean isTransactionFromTwoDifferentRegions = numTransactionsNotInRegion == 2;
        final boolean isTransactionFromTwoDifferentIps = numTransactionsNotWithIp == 2;
        final boolean isTransactionFromMoreThanTwoDifferentRegions = numTransactionsNotInRegion > 2;
        final boolean isTransactionFromMoreThanTwoDifferentIps = numTransactionsNotWithIp > 2;
        
        switch (result) {
            case ALLOWED:
                if(isBlackListedAddress && isBlackListedCardNumber && isTransactionFromMoreThanTwoDifferentRegions && isTransactionFromMoreThanTwoDifferentIps) {
                    tb.result(PROHIBITED).info("card-number, ip, ip-correlation, region-correlation");
                } else if (isTransactionFromMoreThanTwoDifferentRegions && isTransactionFromMoreThanTwoDifferentIps) {
                    tb.result(PROHIBITED).info("ip-correlation, region-correlation");
                } else if (isBlackListedAddress && isBlackListedCardNumber) {
                    tb.result(PROHIBITED).info("card-number, ip");
                } else if (isTransactionFromTwoDifferentRegions && isTransactionFromTwoDifferentIps) {
                    tb.result(MANUAL_PROCESSING).info("ip-correlation, region-correlation");
                } else if (isTransactionFromTwoDifferentRegions) {
                    tb.result(MANUAL_PROCESSING).info("region-correlation");
                } else if (isTransactionFromTwoDifferentIps) {
                    tb.result(MANUAL_PROCESSING).info("ip-correlation");
                } else if (isTransactionFromMoreThanTwoDifferentRegions) {
                    tb.result(PROHIBITED).info("region-correlation");
                } else if (isTransactionFromMoreThanTwoDifferentIps) {
                    tb.result(PROHIBITED).info("ip-correlation");
                } else if (isBlackListedCardNumber) {
                    tb.result(PROHIBITED).info("card-number");
                } else if (isBlackListedAddress) {
                    tb.result(PROHIBITED).info("ip");
                } else {
                    tb.result(ALLOWED).info("none");
                }
                break;
            case PROHIBITED:
                tb.result(PROHIBITED);
                if (isBlackListedAddress && isBlackListedCardNumber && isTransactionFromMoreThanTwoDifferentRegions && isTransactionFromMoreThanTwoDifferentIps) {
                    tb.info("amount, card-number, ip, ip-correlation, region-correlation");
                } else if (isBlackListedAddress && isBlackListedCardNumber && isTransactionFromMoreThanTwoDifferentIps) {
                    tb.info("amount, card-number, ip, ip-correlation");
                } else if (isBlackListedAddress && isBlackListedCardNumber && isTransactionFromMoreThanTwoDifferentRegions) {
                    tb.info("amount, card-number, ip, region-correlation");
                } else if (isBlackListedAddress && isBlackListedCardNumber) {
                    tb.info("amount, card-number, ip");
                } else if (isBlackListedCardNumber) {
                    tb.info("amount, card-number");
                } else if (isBlackListedAddress) {
                    tb.info("amount, ip");
                } else if (isTransactionFromMoreThanTwoDifferentRegions) {
                    tb.info("amount, region-correlation");
                } else if (isTransactionFromMoreThanTwoDifferentIps) {
                    tb.info("amount, ip-correlation");
                } else {
                    tb.info("amount");
                }
                break;
            case MANUAL_PROCESSING:
                tb.result(MANUAL_PROCESSING);
                if (isTransactionFromTwoDifferentRegions && isTransactionFromTwoDifferentIps && isBlackListedAddress && isBlackListedCardNumber) {
                    tb.result(PROHIBITED).info("card-number, ip, ip-correlation, region-correlation");
                } else if (isBlackListedCardNumber && isBlackListedAddress) {
                    tb.result(PROHIBITED).info("card-number, ip");
                } else if (isBlackListedCardNumber) {
                    tb.result(PROHIBITED).info("card-number");
                } else if (isBlackListedAddress) {
                    tb.result(PROHIBITED).info("ip");
                } else if (isTransactionFromTwoDifferentRegions && isTransactionFromTwoDifferentIps) {
                    tb.info("ip-correlation, region-correlation");
                } else if (isTransactionFromTwoDifferentRegions) {
                    tb.info("region-correlation");
                } else if (isTransactionFromTwoDifferentIps) {
                    tb.info("ip-correlation");
                } else if (isTransactionFromMoreThanTwoDifferentRegions) {
                    tb.result(PROHIBITED).info("region-correlation");
                } else if (isTransactionFromMoreThanTwoDifferentIps) {
                    tb.result(PROHIBITED).info("ip-correlation");
                } else {
                    tb.info("amount");
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + result);
        }

        TransactionResponse resp = tb.build();
        transaction.setResult(resp.getResult());
        transactionRepository.save(transaction);
        return resp;
    }

    @Override
    public List<Transaction> findAlLByNumberAndDateBetweenAndRegionNot(String number, LocalDateTime before, LocalDateTime now, Region region) {
        return this.transactionRepository.findAlLByNumberAndDateBetweenAndRegionNot(number, before, now, region);
    }

    @Override
    public List<Transaction> findAllByNumberAndDateIsBetweenAndIpNot(String number, LocalDateTime before, LocalDateTime now, String ip) {
        return this.transactionRepository.findAllByNumberAndDateIsBetweenAndIpNot(number, before, now, ip);
    }

    @Override
    @Transactional
    public Transaction updateTransactionFeedback(FeedbackUpdateRequest request)
            throws TransactionNotFoundException, TransactionFeedbackAlreadyExistException, IllegalFeedbackException, TransactionFeedbackUpdateException {

        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new TransactionNotFoundException(String.format("Transaction for id = %s not found", request.getTransactionId())));

        var requestFeedback = TransactionValidationResult.parse(request.getFeedback());
        final TransactionValidationResult result = transaction.getResult();
        final Long amount = transaction.getAmount();

        if (!transaction.getFeedbackString().isEmpty()) {
            throw new TransactionFeedbackAlreadyExistException("Feedback for specified transaction already set!");
        }

        if (result.equals(requestFeedback)) {
            throw new TransactionFeedbackUpdateException("Transaction Feedback and Transaction Validity are equal!");
        }

        switch (requestFeedback) {
            case ALLOWED:
                if (result.equals(MANUAL_PROCESSING)) {
                    MAX_ALLOWED = increaseLimit(MAX_ALLOWED, amount);
                } else if (result.equals(PROHIBITED)) {
                    MAX_ALLOWED = increaseLimit(MAX_ALLOWED, amount);
                    MAX_MANUAL_PROCESSING = increaseLimit(MAX_MANUAL_PROCESSING, amount);
                }
                break;
            case MANUAL_PROCESSING:
                if (result.equals(ALLOWED)) {
                    MAX_ALLOWED = decreaseLimit(MAX_ALLOWED, amount);
                } else if (result.equals(PROHIBITED)) {
                    MAX_MANUAL_PROCESSING = increaseLimit(MAX_MANUAL_PROCESSING, amount);
                }
                break;
            case PROHIBITED:
                if (result.equals(ALLOWED)) {
                    MAX_ALLOWED = decreaseLimit(MAX_ALLOWED, amount);
                    MAX_MANUAL_PROCESSING = decreaseLimit(MAX_MANUAL_PROCESSING, amount);
                } else if (result.equals(MANUAL_PROCESSING)) {
                    MAX_MANUAL_PROCESSING = decreaseLimit(MAX_MANUAL_PROCESSING, amount);
                }
                break;
        }

        transaction.setFeedback(requestFeedback);

        return transactionRepository.save(transaction);
    }

    @Override
    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    @Override
    public List<Transaction> findAllByNumber(String number) throws InvalidNumberException, TransactionNotFoundException {

        if (AntiFraudUtil.isValidNumber().negate().test(number)) {
            throw new InvalidNumberException("card number validation failed");
        }

        final List<Transaction> transactions = transactionRepository.findAllByNumber(number);

        if (transactions.isEmpty()) {
            throw new TransactionNotFoundException(String.format("No transaction for number = %s found", number));
        }
        return transactions;
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

    private long increaseLimit(long currentLimit, long amount) {
        return (long) Math.ceil(0.8 * currentLimit + 0.2 * amount);
    }

    private long decreaseLimit(long currentLimit, long amount) {
        return (long) Math.ceil(0.8 * currentLimit - 0.2 * amount);
    }
}
