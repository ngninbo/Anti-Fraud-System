package antifraud.model;

import antifraud.domain.TransactionValidationResult;
import antifraud.util.AntiFraudValidator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
@SequenceGenerator(name = "transaction_sequence", sequenceName = "TransactionSequence")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_sequence")
    private Long id;

    @NotNull
    private Long amount;

    @Enumerated(EnumType.STRING)
    private TransactionValidationResult status;

    @NotEmpty
    private String ip;

    @NotEmpty
    private String number;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id")
    private User user;

    @AssertTrue(message = "The transaction amount must be greater than 0")
    public boolean isNonNegativeAmount() {
        return amount != null && amount > 0;
    }

    @AssertTrue(message = "ip address is not valid")
    @JsonIgnore
    public boolean isValidIp() {
        return ip != null && AntiFraudValidator.isValidIP().test(ip);
    }

    @AssertTrue(message = "Invalid card number in request!")
    @JsonIgnore
    public boolean isValid() {
        return number != null && AntiFraudValidator.isValidNumber().test(number);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Transaction that = (Transaction) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
