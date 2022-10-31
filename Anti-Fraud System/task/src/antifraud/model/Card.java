package antifraud.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@SequenceGenerator(name = "card_sequence", sequenceName = "CardSequence")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "card_sequence")
    @Column(name = "card_id")
    private Long id;
    private String number;
    private Long maxAllowed;
    private Long maxManual;

    public Card(String cardNumber, long maxAllowed, long maxManual) {
        this.number = cardNumber;
        this.maxAllowed = maxAllowed;
        this.maxManual = maxManual;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Card card = (Card) o;
        return id != null && Objects.equals(id, card.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
