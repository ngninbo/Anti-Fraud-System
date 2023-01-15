package antifraud.domain;

import antifraud.exception.TransactionDateParsingException;
import antifraud.util.AntiFraudUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.AssertTrue;
import java.time.LocalDateTime;
import java.util.Arrays;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class TransactionDto {

    private Long amount;
    private String ip;
    private String number;
    private String region;
    private String date;

    @AssertTrue(message = "The transaction amount must be greater than 0")
    public boolean isNonNegativeAmount() {
        return amount != null && amount > 0;
    }

    @AssertTrue(message = "ip address is not valid")
    @JsonIgnore
    public boolean isValidIp() {
        return ip != null && AntiFraudUtil.isValidIP().test(ip);
    }

    @AssertTrue(message = "Invalid card number in request!")
    @JsonIgnore
    public boolean isValid() {
        return number != null && AntiFraudUtil.isValidNumber().test(number);
    }

    @AssertTrue(message = "Wrong region!")
    public boolean isValidRegion() {
        return region != null && Arrays.stream(Region.values()).anyMatch(el -> el.name().equals(region));
    }

    public LocalDateTime getDate() {

        try {
            return LocalDateTime.parse(date);
        } catch (Exception e) {
            throw new TransactionDateParsingException("Invalid date format");
        }
    }
}
