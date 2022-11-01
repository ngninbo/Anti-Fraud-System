package antifraud.model;

import antifraud.domain.Region;
import antifraud.domain.TransactionValidationResult;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
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
    @JsonProperty(value = "transactionId")
    private Long id;

    @NotNull
    private Long amount;

    @NotEmpty
    private String ip;

    @NotEmpty
    private String number;

    @Enumerated(EnumType.STRING)
    private Region region;

    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private TransactionValidationResult result;

    @Enumerated(EnumType.STRING)
    private TransactionValidationResult feedback;

    public Transaction(Long amount, String ip, String number, Region region, LocalDateTime date) {
        this.amount = amount;
        this.ip = ip;
        this.number = number;
        this.region = region;
        this.date = date;
    }

    @JsonProperty("feedback")
    public String getFeedbackString() {
        return feedback == null ? "" : feedback.name();
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
