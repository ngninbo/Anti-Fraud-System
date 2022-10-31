package antifraud.model;

import antifraud.util.AntiFraudUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@SequenceGenerator(name = "stolen_card_sequence", sequenceName = "StolenCardSequence")
public class StolenCard {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stolen_card_sequence")
    @Column(name = "stolen_card_id")
    private Long id;

    @NotEmpty
    private String number;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        StolenCard stolenCard = (StolenCard) o;
        return id != null && Objects.equals(id, stolenCard.id);
    }

    @AssertTrue(message = "Invalid card number in request!")
    @JsonIgnore
    public boolean isValid() {
        return number != null && AntiFraudUtil.isValidNumber().test(number);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
