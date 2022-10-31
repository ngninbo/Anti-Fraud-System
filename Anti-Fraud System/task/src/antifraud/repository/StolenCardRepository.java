package antifraud.repository;

import antifraud.model.StolenCard;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface StolenCardRepository extends CrudRepository<StolenCard, Long> {

    Optional<StolenCard> findByNumber(String number);

    List<StolenCard> findAll();
}
