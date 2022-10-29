package antifraud.service;

import antifraud.domain.Region;
import antifraud.domain.TransactionDto;
import antifraud.domain.TransactionResponse;
import antifraud.domain.TransactionValidationResult;
import antifraud.exception.InvalidRegionException;
import antifraud.exception.TransactionDateParsingException;
import antifraud.exception.UserNotFoundException;
import antifraud.model.Transaction;
import antifraud.repository.TransactionRepository;
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

    private final TransactionRepository transactionRepository;
    private final UserServiceImpl userService;
    private final AddressBlacklistService addressBlacklistService;
    private final CardBlacklistService cardBlacklistService;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, UserServiceImpl userService, AddressBlacklistService addressBlacklistService, CardBlacklistService cardBlacklistService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.addressBlacklistService = addressBlacklistService;
        this.cardBlacklistService = cardBlacklistService;
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

        boolean isBlackListedAddress = addressBlacklistService.isBlacklistedIp(transaction.getIp());
        boolean isBlackListedCardNumber = cardBlacklistService.isBlacklistedCardNumber(transaction.getNumber());
        LocalDateTime lastHour = date.minusHours(1);
        var  numTransactionsNotInRegion = this.findAlLByNumberAndDateBetweenAndRegionNot(transaction.getNumber(), lastHour, date, region).stream().map(Transaction::getRegion).distinct().count();
        var numTransactionsNotWithIp = this.findAllByNumberAndDateIsBetweenAndIpNot(transaction.getNumber(), lastHour, date, transaction.getIp()).stream().map(Transaction::getIp).distinct().count();

        String reason;
        var result = getResult(transaction.getAmount());

        final boolean isTransactionFromTwoDifferentRegions = numTransactionsNotInRegion == 2;
        final boolean isTransactionFromTwoDifferentIps = numTransactionsNotWithIp == 2;
        final boolean isTransactionFromMoreThanTwoDifferentRegions = numTransactionsNotInRegion > 2;
        final boolean isTransactionFromMoreThanTwoDifferentIps = numTransactionsNotWithIp > 2;
        switch (result) {
            case ALLOWED:
                if(isBlackListedAddress && isBlackListedCardNumber && isTransactionFromMoreThanTwoDifferentRegions && isTransactionFromMoreThanTwoDifferentIps) {
                    result = PROHIBITED;
                    reason = "card-number, ip, ip-correlation, region-correlation";
                } else if (isTransactionFromMoreThanTwoDifferentRegions && isTransactionFromMoreThanTwoDifferentIps) {
                    result = PROHIBITED;
                    reason = "ip-correlation, region-correlation";
                } else if (isBlackListedAddress && isBlackListedCardNumber) {
                    result = PROHIBITED;
                    reason = "card-number, ip";
                } else if (isTransactionFromTwoDifferentRegions && isTransactionFromTwoDifferentIps) {
                    result = MANUAL_PROCESSING;
                    reason = "ip-correlation, region-correlation";
                } else if (isTransactionFromTwoDifferentRegions) {
                    result = MANUAL_PROCESSING;
                    reason = "region-correlation";
                } else if (isTransactionFromTwoDifferentIps) {
                    result = MANUAL_PROCESSING;
                    reason = "ip-correlation";
                } else if (isTransactionFromMoreThanTwoDifferentRegions) {
                    result = PROHIBITED;
                    reason = "region-correlation";
                } else if (isTransactionFromMoreThanTwoDifferentIps) {
                    result = PROHIBITED;
                    reason = "ip-correlation";
                } else if (isBlackListedCardNumber) {
                    result = PROHIBITED;
                    reason = "card-number";
                } else if (isBlackListedAddress) {
                    result = PROHIBITED;
                    reason = "ip";
                } else {
                    reason = "none";
                }
                break;
            case PROHIBITED:
                if (isBlackListedAddress && isBlackListedCardNumber && isTransactionFromMoreThanTwoDifferentRegions && isTransactionFromMoreThanTwoDifferentIps) {
                    reason = "amount, card-number, ip, ip-correlation, region-correlation";
                } else if (isBlackListedAddress && isBlackListedCardNumber && isTransactionFromMoreThanTwoDifferentIps) {
                    reason = "amount, card-number, ip, ip-correlation";
                } else if (isBlackListedAddress && isBlackListedCardNumber && isTransactionFromMoreThanTwoDifferentRegions) {
                    reason = "amount, card-number, ip, region-correlation";
                } else if (isBlackListedAddress && isBlackListedCardNumber) {
                    reason = "amount, card-number, ip";
                } else if (isBlackListedCardNumber) {
                    reason = "amount, card-number";
                } else if (isBlackListedAddress) {
                    reason = "amount, ip";
                } else if (isTransactionFromMoreThanTwoDifferentRegions) {
                    reason = "amount, region-correlation";
                } else if (isTransactionFromMoreThanTwoDifferentIps) {
                    reason = "amount, ip-correlation";
                } else {
                    reason = "amount";
                }
                break;
            case MANUAL_PROCESSING:
                if (isTransactionFromTwoDifferentRegions && isTransactionFromTwoDifferentIps && isBlackListedAddress && isBlackListedCardNumber) {
                    result = PROHIBITED;
                    reason = "card-number, ip, ip-correlation, region-correlation";
                } else if (isBlackListedCardNumber && isBlackListedAddress) {
                    result = PROHIBITED;
                    reason = "card-number, ip";
                } else if (isBlackListedCardNumber) {
                    result = PROHIBITED;
                    reason = "card-number";
                } else if (isBlackListedAddress) {
                    result = PROHIBITED;
                    reason = "ip";
                } else if (isTransactionFromTwoDifferentRegions && isTransactionFromTwoDifferentIps) {
                    reason = "ip-correlation, region-correlation";
                } else if (isTransactionFromTwoDifferentRegions) {
                    reason = "region-correlation";
                } else if (isTransactionFromTwoDifferentIps) {
                    reason = "ip-correlation";
                } else if (isTransactionFromMoreThanTwoDifferentRegions) {
                    result = PROHIBITED;
                    reason = "region-correlation";
                } else if (isTransactionFromMoreThanTwoDifferentIps) {
                    result = PROHIBITED;
                    reason = "ip-correlation";
                } else {
                    reason = "amount";
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + result);
        }

        transaction.setStatus(result);
        transactionRepository.save(transaction);
        return TransactionResponse.builder().result(result).info(reason).build();
    }

    @Override
    public List<Transaction> findAlLByNumberAndDateBetweenAndRegionNot(String number, LocalDateTime before, LocalDateTime now, Region region) {
        return this.transactionRepository.findAlLByNumberAndDateBetweenAndRegionNot(number, before, now, region);
    }

    @Override
    public List<Transaction> findAllByNumberAndDateIsBetweenAndIpNot(String number, LocalDateTime before, LocalDateTime now, String ip) {
        return this.transactionRepository.findAllByNumberAndDateIsBetweenAndIpNot(number, before, now, ip);
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
