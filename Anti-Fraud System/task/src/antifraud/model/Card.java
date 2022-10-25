package antifraud.model;

import antifraud.util.AntiFraudValidator;
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
@SequenceGenerator(name = "card_sequence", sequenceName = "CardSequence")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "card_sequence")
    @Column(name = "card_id")
    private Long id;

    @NotEmpty
    private String number;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Card card = (Card) o;
        return id != null && Objects.equals(id, card.id);
    }

    @AssertTrue(message = "Invalid card number in request!")
    @JsonIgnore
    public boolean isValid() {
        return number != null && AntiFraudValidator.isValidNumber().test(number);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
