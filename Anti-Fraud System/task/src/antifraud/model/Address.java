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
@Table(name = "addresses")
@SequenceGenerator(name = "address_sequence", sequenceName = "AddressSequence")
public class Address {

    @Id
    @Column(name = "address_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_sequence")
    private Long id;
    @NotEmpty(message = "ip address must not be empty")
    private String ip;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Address address = (Address) o;
        return id != null && Objects.equals(id, address.id);
    }

    @AssertTrue(message = "ip address is not valid")
    @JsonIgnore
    public boolean isValidIp() {
        return ip != null && AntiFraudUtil.isValidIP().test(ip);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
