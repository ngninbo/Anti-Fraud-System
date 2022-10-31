package antifraud.service;

import antifraud.rest.AddressDeletionResponse;
import antifraud.exception.AddressAlreadyExistException;
import antifraud.exception.AddressNotFoundException;
import antifraud.model.Address;

import java.util.List;

public interface SuspiciousIpService {

    List<Address> findAll();
    Address create(Address address) throws AddressAlreadyExistException;

    AddressDeletionResponse removeIP(String ip) throws AddressNotFoundException;

    boolean isBlacklistedIp(String ip);
}
