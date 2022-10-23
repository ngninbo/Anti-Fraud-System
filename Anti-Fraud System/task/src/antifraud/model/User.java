package antifraud.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "users")
@SequenceGenerator(name = "sequence", sequenceName = "UserSeq")
public class User {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private Long id;
    @NotEmpty(message = "name must not be empty")
    private String name;
    @NotEmpty(message = "username must not be empty")
    private String username;
    @NotEmpty(message = "password must not be empty")
    private String password;

    private String role;

    @OneToMany(mappedBy="user", orphanRemoval = true)
    private Set<Transaction> transactions;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
