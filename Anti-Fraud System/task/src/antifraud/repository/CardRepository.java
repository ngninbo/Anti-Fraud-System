package antifraud.repository;

import antifraud.model.Card;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CardRepository extends CrudRepository<Card, Long> {
    Optional<Card> findByNumber(String cardNumber);
}
