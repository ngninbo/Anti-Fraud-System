package antifraud.service;

import antifraud.domain.*;
import antifraud.exception.*;
import antifraud.model.Transaction;
import antifraud.repository.TransactionRepository;
import antifraud.rest.FeedbackUpdateRequest;
import antifraud.rest.TransactionResponse;
import antifraud.util.AntiFraudUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;

import static antifraud.domain.TransactionValidationResult.*;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final SuspiciousIpService suspiciousIpService;
    private final StolenCardService stolenCardService;
    private final CardService cardService;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  SuspiciousIpService suspiciousIpService, StolenCardService stolenCardService, CardService cardService) {
        this.transactionRepository = transactionRepository;
        this.suspiciousIpService = suspiciousIpService;
        this.stolenCardService = stolenCardService;
        this.cardService = cardService;
    }

    @Override
    @Transactional
    public TransactionResponse validate(TransactionDto dto) throws InvalidRegionException, DateTimeException, TransactionDateParsingException {

        final Region region = Region.toRegion(dto.getRegion());
        final LocalDateTime date;
        try {
            date = LocalDateTime.parse(dto.getDate());
        } catch (Exception e) {
            throw new TransactionDateParsingException("Invalid date format");
        }
        Transaction transaction = new Transaction(dto.getAmount(), dto.getIp(), dto.getNumber(), region, date);

        boolean isBlackListedAddress = suspiciousIpService.isBlacklistedIp(transaction.getIp());
        boolean isBlackListedCardNumber = stolenCardService.isBlacklistedCardNumber(transaction.getNumber());
        LocalDateTime lastHour = date.minusHours(1);
        var  numTransactionsNotInRegion = this.findAlLByNumberAndDateBetweenAndRegionNot(transaction.getNumber(), lastHour, date, region).stream().map(Transaction::getRegion).distinct().count();
        var numTransactionsNotWithIp = this.findAllByNumberAndDateIsBetweenAndIpNot(transaction.getNumber(), lastHour, date, transaction.getIp()).stream().map(Transaction::getIp).distinct().count();
        
        var result = getResult(transaction.getAmount(), transaction.getNumber());
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

        cardService.processLimits(transaction.getNumber(), amount, result, requestFeedback);
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

    private TransactionValidationResult getResult(Long amount, String cardNumber) {
        return this.cardService.processAmount(amount, cardNumber);
    }
}
