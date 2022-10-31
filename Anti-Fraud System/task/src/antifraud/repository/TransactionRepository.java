package antifraud.repository;

import antifraud.domain.Region;
import antifraud.model.Transaction;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    List<Transaction> findAlLByNumberAndDateBetweenAndRegionNot(String number, LocalDateTime start, LocalDateTime end, Region region);
    List<Transaction> findAllByNumberAndDateIsBetweenAndIpNot(String number, LocalDateTime start, LocalDateTime end, String ip);
    List<Transaction> findAll();
    List<Transaction> findAllByNumber(String number);
}
