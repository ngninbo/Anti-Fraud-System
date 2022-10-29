package antifraud.service;

import antifraud.domain.AddressDeletionResponse;
import antifraud.exception.AddressAlreadyExistException;
import antifraud.exception.AddressNotFoundException;
import antifraud.model.Address;

import java.util.List;

public interface AddressBlacklistService {

    List<Address> findAll();
    Address create(Address address) throws AddressAlreadyExistException;

    AddressDeletionResponse removeIP(String ip) throws AddressNotFoundException;

    boolean isBlacklistedIp(String ip);
}
