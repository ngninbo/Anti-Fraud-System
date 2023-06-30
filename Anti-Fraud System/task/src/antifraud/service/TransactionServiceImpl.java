package antifraud.service;

import antifraud.domain.*;
import antifraud.exception.*;
import antifraud.model.Transaction;
import antifraud.repository.TransactionRepository;
import antifraud.rest.FeedbackUpdateRequest;
import antifraud.rest.TransactionResponse;
import antifraud.util.AntiFraudUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiFunction;

import static antifraud.domain.TransactionValidationResult.*;

@Service
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private static final String CARD_NUMBER = "card-number";
    private static final String IP_CORRELATION = "ip-correlation";
    private static final String REGION_CORRELATION = "region-correlation";
    private static final String AMOUNT = "amount";
    private static final String IP = "ip";
    private final TransactionRepository transactionRepository;
    private final SuspiciousIpService suspiciousIpService;
    private final StolenCardService stolenCardService;
    private final CardService cardService;

    @Override
    @Transactional
    public TransactionResponse validate(TransactionDto dto) {

        final Region region = Region.toRegion(dto.getRegion());
        final LocalDateTime date = dto.getDate();
        Transaction transaction = new Transaction(dto.getAmount(), dto.getIp(), dto.getNumber(), region, date);
        TransactionResponse resp = getTransactionResponse(transaction);
        transaction.setResult(resp.getResult());
        transactionRepository.save(transaction);
        return resp;
    }

    @Override
    @Transactional
    public Transaction updateTransactionFeedback(FeedbackUpdateRequest request) {

        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new TransactionNotFoundException(String.format("Transaction for id = %s not found", request.getTransactionId())));

        TransactionValidationResult requestFeedback = TransactionValidationResult.parse(request.getFeedback());
        final TransactionValidationResult result = transaction.getResult();

        if (!transaction.getFeedbackString().isEmpty()) {
            throw new TransactionFeedbackAlreadyExistException("Feedback for specified transaction already set!");
        }

        if (result.equals(requestFeedback)) {
            throw new TransactionFeedbackUpdateException("Transaction Feedback and Transaction Validity are equal!");
        }

        cardService.processLimits(transaction, requestFeedback);
        transaction.setFeedback(requestFeedback);

        return transactionRepository.save(transaction);
    }

    @Override
    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    @Override
    public List<Transaction> findAllByNumber(String number) {

        if (AntiFraudUtil.isValidNumber().negate().test(number)) {
            throw new InvalidNumberException("card number validation failed");
        }

        final List<Transaction> transactions = transactionRepository.findAllByNumber(number);

        if (transactions.isEmpty()) {
            throw new TransactionNotFoundException(String.format("No transaction for number = %s found", number));
        }
        return transactions;
    }

    @Override
    public List<Transaction> findAlLByNumberAndDateBetweenAndRegionNot(String number, LocalDateTime before, LocalDateTime now, Region region) {
        return this.transactionRepository.findAlLByNumberAndDateBetweenAndRegionNot(number, before, now, region);
    }

    @Override
    public List<Transaction> findAllByNumberAndDateIsBetweenAndIpNot(String number, LocalDateTime before, LocalDateTime now, String ip) {
        return this.transactionRepository.findAllByNumberAndDateIsBetweenAndIpNot(number, before, now, ip);
    }

    private TransactionResponse getTransactionResponse(Transaction transaction) {
        var result = cardService.processAmount(transaction.getAmount(), transaction.getNumber());
        return new TransactionServiceResponse(transaction).setResult(result).get();
    }

    protected class TransactionServiceResponse {

        private boolean isTransactionFromTwoDifferentRegions;
        private boolean isTransactionFromTwoDifferentIps;
        private boolean isTransactionFromMoreThanTwoDifferentRegions;
        private boolean isTransactionFromMoreThanTwoDifferentIps;

        private boolean isBlackListedAddress;
        private boolean isBlackListedCardNumber;

        private final TransactionResponse.TransactionResponseBuilder tb = TransactionResponse.builder();

        private TransactionValidationResult result;

        public TransactionServiceResponse(Transaction transaction) {
            setUp(transaction);
        }

        private void setUp(Transaction transaction) {
            final String ip = transaction.getIp();
            final String number = transaction.getNumber();
            Region region = transaction.getRegion();
            LocalDateTime date = transaction.getDate();
            LocalDateTime lastHour = date.minusHours(1);

            long numTransactionsNotInRegion = findAlLByNumberAndDateBetweenAndRegionNot(number,
                    lastHour, date, region).stream().map(Transaction::getRegion).distinct().count();

            long numTransactionsNotWithIp = findAllByNumberAndDateIsBetweenAndIpNot(number,
                    lastHour, date, ip).stream().map(Transaction::getIp).distinct().count();

            isTransactionFromTwoDifferentRegions = equalTwo(numTransactionsNotInRegion);
            isTransactionFromTwoDifferentIps = equalTwo(numTransactionsNotWithIp);
            isTransactionFromMoreThanTwoDifferentRegions = isGreaterThanTwo(numTransactionsNotInRegion);
            isTransactionFromMoreThanTwoDifferentIps = isGreaterThanTwo(numTransactionsNotWithIp);

            isBlackListedAddress = suspiciousIpService.isBlacklistedIp(ip);
            isBlackListedCardNumber = stolenCardService.isBlacklistedCardNumber(number);
        }

        public TransactionServiceResponse setResult(TransactionValidationResult result) {
            this.result = result;
            return this;
        }

        public TransactionResponse get() {

            switch (result) {
                case ALLOWED:
                    return doAllowed();
                case MANUAL_PROCESSING:
                    return doManual();
                case PROHIBITED:
                    return doProhibit();
                default:
                    throw new IllegalStateException("Unexpected value: " + result);
            }
        }

        private TransactionResponse doAllowed() {
            if (prohibit()) {
                tb.result(PROHIBITED).info(joinReasons(CARD_NUMBER, IP, IP_CORRELATION, REGION_CORRELATION));
            } else if (moreThanTwoIpsAndRegions()) {
                tb.result(PROHIBITED).info(joinReasons(IP_CORRELATION, REGION_CORRELATION));
            } else if (cardAndAddressBlacklisted()) {
                tb.result(PROHIBITED).info(joinReasons(CARD_NUMBER, IP));
            } else if (isTransactionFromMoreThanTwoDifferentRegions) {
                tb.result(PROHIBITED).info(REGION_CORRELATION);
            } else if (isTransactionFromMoreThanTwoDifferentIps) {
                tb.result(PROHIBITED).info(IP_CORRELATION);
            } else if (isBlackListedCardNumber) {
                tb.result(PROHIBITED).info(CARD_NUMBER);
            } else if (isBlackListedAddress) {
                tb.result(PROHIBITED).info(IP);
            } else if (ipAndRegionCorrelation()) {
                tb.result(MANUAL_PROCESSING).info(joinReasons(IP_CORRELATION, REGION_CORRELATION));
            } else if (isTransactionFromTwoDifferentRegions) {
                tb.result(MANUAL_PROCESSING).info(REGION_CORRELATION);
            } else if (isTransactionFromTwoDifferentIps) {
                tb.result(MANUAL_PROCESSING).info(IP_CORRELATION);
            } else {
                tb.result(ALLOWED).info("none");
            }

            return tb.build();
        }

        private TransactionResponse doProhibit() {
            tb.result(result);
            if (prohibit()) {
                tb.info(joinReasons(AMOUNT, CARD_NUMBER, IP, IP_CORRELATION, REGION_CORRELATION));
            } else if (check().apply(cardAndAddressBlacklisted(), isTransactionFromMoreThanTwoDifferentIps)) {
                tb.info(joinReasons(AMOUNT, CARD_NUMBER, IP, IP_CORRELATION));
            } else if (check().apply(cardAndAddressBlacklisted(), isTransactionFromMoreThanTwoDifferentRegions)) {
                tb.info(joinReasons(AMOUNT, CARD_NUMBER, IP, REGION_CORRELATION));
            } else if (cardAndAddressBlacklisted()) {
                tb.info(joinReasons(AMOUNT, CARD_NUMBER, IP));
            } else if (isBlackListedCardNumber) {
                tb.info(joinReasons(AMOUNT, CARD_NUMBER));
            } else if (isBlackListedAddress) {
                tb.info(joinReasons(AMOUNT, IP));
            } else if (isTransactionFromMoreThanTwoDifferentRegions) {
                tb.info(joinReasons(AMOUNT, REGION_CORRELATION));
            } else if (isTransactionFromMoreThanTwoDifferentIps) {
                tb.info(joinReasons(AMOUNT, IP_CORRELATION));
            } else {
                tb.info(AMOUNT);
            }

            return tb.build();
        }

        private TransactionResponse doManual() {
            tb.result(result);
            if (prohibit()) {
                tb.result(PROHIBITED).info(joinReasons(CARD_NUMBER, IP, IP_CORRELATION, REGION_CORRELATION));
            } else if (moreThanTwoIpsAndRegions()) {
                tb.result(PROHIBITED).info(joinReasons(IP_CORRELATION, REGION_CORRELATION));
            } else if (cardAndAddressBlacklisted()) {
                tb.result(PROHIBITED).info(joinReasons(CARD_NUMBER, IP));
            } else if (isTransactionFromMoreThanTwoDifferentRegions) {
                tb.result(PROHIBITED).info(REGION_CORRELATION);
            } else if (isTransactionFromMoreThanTwoDifferentIps) {
                tb.result(PROHIBITED).info(IP_CORRELATION);
            } else if (isBlackListedCardNumber) {
                tb.result(PROHIBITED).info(CARD_NUMBER);
            } else if (isBlackListedAddress) {
                tb.result(PROHIBITED).info(IP);
            } else if (ipAndRegionCorrelation()) {
                tb.info(joinReasons(IP_CORRELATION, REGION_CORRELATION));
            } else if (isTransactionFromTwoDifferentRegions) {
                tb.info(REGION_CORRELATION);
            } else if (isTransactionFromTwoDifferentIps) {
                tb.info(IP_CORRELATION);
            } else {
                tb.info(AMOUNT);
            }

            return tb.build();
        }

        private String joinReasons(String... reasons) {
            return String.join(", ", reasons);
        }

        private boolean prohibit() {
            return check().apply(cardAndAddressBlacklisted(), ipAndRegionCorrelation());
        }

        private boolean ipAndRegionCorrelation() {
            return check().apply(isTransactionFromTwoDifferentIps, isTransactionFromTwoDifferentRegions);
        }

        private boolean cardAndAddressBlacklisted() {
            return check().apply(isBlackListedCardNumber, isBlackListedAddress);
        }

        private boolean moreThanTwoIpsAndRegions() {
            return check().apply(isTransactionFromMoreThanTwoDifferentIps, isTransactionFromMoreThanTwoDifferentRegions);
        }

        private BiFunction<Boolean, Boolean, Boolean> check() {
            return (a, b) -> a && b;
        }

        private boolean isGreaterThanTwo(long value) {
            return value > 2;
        }

        private boolean equalTwo(long value) {
            return value == 2;
        }
    }
}
