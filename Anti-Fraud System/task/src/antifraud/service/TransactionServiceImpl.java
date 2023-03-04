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

import static antifraud.domain.TransactionValidationResult.*;

@Service
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {

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

        boolean isBlackListedAddress;
        boolean isBlackListedCardNumber;

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

            var  numTransactionsNotInRegion = findAlLByNumberAndDateBetweenAndRegionNot(number,
                    lastHour, date, region).stream().map(Transaction::getRegion).distinct().count();

            var numTransactionsNotWithIp = findAllByNumberAndDateIsBetweenAndIpNot(number,
                    lastHour, date, ip).stream().map(Transaction::getIp).distinct().count();

            isTransactionFromTwoDifferentRegions = numTransactionsNotInRegion == 2;
            isTransactionFromTwoDifferentIps = numTransactionsNotWithIp == 2;
            isTransactionFromMoreThanTwoDifferentRegions = numTransactionsNotInRegion > 2;
            isTransactionFromMoreThanTwoDifferentIps = numTransactionsNotWithIp > 2;

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
            if (isBlackListedCardNumber && isBlackListedAddress && isTransactionFromMoreThanTwoDifferentIps && isTransactionFromMoreThanTwoDifferentRegions) {
                tb.result(PROHIBITED).info("card-number, ip, ip-correlation, region-correlation");
            } else if (isTransactionFromMoreThanTwoDifferentIps && isTransactionFromMoreThanTwoDifferentRegions) {
                tb.result(PROHIBITED).info("ip-correlation, region-correlation");
            } else if (isBlackListedCardNumber && isBlackListedAddress) {
                tb.result(PROHIBITED).info("card-number, ip");
            } else if (isTransactionFromMoreThanTwoDifferentRegions) {
                tb.result(PROHIBITED).info("region-correlation");
            } else if (isTransactionFromMoreThanTwoDifferentIps) {
                tb.result(PROHIBITED).info("ip-correlation");
            } else if (isBlackListedCardNumber) {
                tb.result(PROHIBITED).info("card-number");
            } else if (isBlackListedAddress) {
                tb.result(PROHIBITED).info("ip");
            } else if (isTransactionFromTwoDifferentRegions && isTransactionFromTwoDifferentIps) {
                tb.result(MANUAL_PROCESSING).info("ip-correlation, region-correlation");
            } else if (isTransactionFromTwoDifferentRegions) {
                tb.result(MANUAL_PROCESSING).info("region-correlation");
            } else if (isTransactionFromTwoDifferentIps) {
                tb.result(MANUAL_PROCESSING).info("ip-correlation");
            } else {
                tb.result(ALLOWED).info("none");
            }

            return tb.build();
        }

        private TransactionResponse doProhibit() {
            tb.result(result);
            if (isBlackListedAddress && isBlackListedCardNumber && isTransactionFromMoreThanTwoDifferentIps && isTransactionFromMoreThanTwoDifferentRegions) {
                tb.info("amount, card-number, ip, ip-correlation, region-correlation");
            } else if (isBlackListedCardNumber && isBlackListedAddress && isTransactionFromMoreThanTwoDifferentIps) {
                tb.info("amount, card-number, ip, ip-correlation");
            } else if (isBlackListedCardNumber && isBlackListedAddress && isTransactionFromMoreThanTwoDifferentRegions) {
                tb.info("amount, card-number, ip, region-correlation");
            } else if (isBlackListedCardNumber && isBlackListedAddress) {
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

            return tb.build();
        }

        private TransactionResponse doManual() {
            tb.result(result);
            if (isBlackListedCardNumber && isBlackListedAddress && isTransactionFromTwoDifferentIps && isTransactionFromTwoDifferentRegions) {
                tb.result(PROHIBITED).info("card-number, ip, ip-correlation, region-correlation");
            } else if (isTransactionFromMoreThanTwoDifferentIps && isTransactionFromMoreThanTwoDifferentRegions) {
                tb.result(PROHIBITED).info("ip-correlation, region-correlation");
            } else if (isBlackListedCardNumber && isBlackListedAddress) {
                tb.result(PROHIBITED).info("card-number, ip");
            } else if (isTransactionFromMoreThanTwoDifferentRegions) {
                tb.result(PROHIBITED).info("region-correlation");
            } else if (isTransactionFromMoreThanTwoDifferentIps) {
                tb.result(PROHIBITED).info("ip-correlation");
            } else if (isBlackListedCardNumber) {
                tb.result(PROHIBITED).info("card-number");
            } else if (isBlackListedAddress) {
                tb.result(PROHIBITED).info("ip");
            } else if (isTransactionFromTwoDifferentIps && isTransactionFromTwoDifferentRegions) {
                tb.info("ip-correlation, region-correlation");
            } else if (isTransactionFromTwoDifferentRegions) {
                tb.info("region-correlation");
            } else if (isTransactionFromTwoDifferentIps) {
                tb.info("ip-correlation");
            } else {
                tb.info("amount");
            }

            return tb.build();
        }
    }
}
