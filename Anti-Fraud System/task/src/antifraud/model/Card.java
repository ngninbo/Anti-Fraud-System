package antifraud.model;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.ColumnDefault;

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

    private static transient final long MAX_ALLOWED = 200L;
    private static transient final long MAX_MANUAL = 1500L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "card_sequence")
    @Column(name = "card_id")
    private Long id;
    private String number;
    @Column(name = "max_allowed")
    @ColumnDefault(value = "250")
    private Long maxAllowed;
    @Column(name = "max_manual")
    private Long maxManual;

    {
        this.maxAllowed = MAX_ALLOWED;
        this.maxManual = MAX_MANUAL;
    }

    public Card(String cardNumber) {
        this.number = cardNumber;
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
