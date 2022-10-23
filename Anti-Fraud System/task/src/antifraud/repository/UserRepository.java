package antifraud.repository;

import antifraud.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findUsersByUsernameIgnoreCase(String name);
    List<User> findAll();
}
