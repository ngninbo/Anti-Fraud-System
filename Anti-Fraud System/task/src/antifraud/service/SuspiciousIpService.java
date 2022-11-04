package antifraud.service;

import antifraud.rest.AddressDeletionResponse;
import antifraud.model.Address;

import java.util.List;

public interface SuspiciousIpService {

    List<Address> findAll();
    Address create(Address address);

    AddressDeletionResponse removeIP(String ip);

    boolean isBlacklistedIp(String ip);
}
