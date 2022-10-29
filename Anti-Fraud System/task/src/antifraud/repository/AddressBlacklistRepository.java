package antifraud.repository;

import antifraud.model.Address;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AddressBlacklistRepository extends CrudRepository<Address, Long> {

    Optional<Address> findByIp(String ip);
    List<Address> findAll();
}
