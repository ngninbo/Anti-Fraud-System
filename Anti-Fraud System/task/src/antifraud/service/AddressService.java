package antifraud.service;

import antifraud.domain.AddressDeletionResponse;
import antifraud.exception.AddressAlreadyExistException;
import antifraud.exception.AddressNotFoundException;
import antifraud.model.Address;

import java.util.List;
import java.util.Optional;

public interface AddressService {

    List<Address> findAll();
    Address create(Address address) throws AddressAlreadyExistException;

    AddressDeletionResponse removeIP(String ip) throws AddressNotFoundException;

    Optional<Address> findByIP(String ip);
}
