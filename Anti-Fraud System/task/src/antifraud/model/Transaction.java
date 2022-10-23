package antifraud.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @NotNull
    private Long amount;

    @AssertTrue(message = "The transaction amount must be greater than 0")
    public boolean isNonNegativeAmount() {
        return amount != null && amount > 0;
    }
}
