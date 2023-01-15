package antifraud.service;

import antifraud.exception.InvalidIpException;
import antifraud.rest.AddressDeletionResponse;
import antifraud.exception.AddressAlreadyExistException;
import antifraud.exception.AddressNotFoundException;
import antifraud.model.Address;
import antifraud.repository.SuspiciousIpRepository;
import antifraud.util.AntiFraudUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class SuspiciousIpServiceImpl implements SuspiciousIpService {

    private final SuspiciousIpRepository suspiciousIpRepository;

    @Autowired
    public SuspiciousIpServiceImpl(SuspiciousIpRepository suspiciousIpRepository) {
        this.suspiciousIpRepository = suspiciousIpRepository;
    }

    @Override
    public List<Address> findAll() {
        return suspiciousIpRepository.findAll();
    }

    @Override
    @Transactional
    public Address create(Address address) throws AddressAlreadyExistException {

        var addressOptional = suspiciousIpRepository.findByIp(address.getIp());

        if (addressOptional.isPresent()) {
            throw new AddressAlreadyExistException("IP address already exist!");
        }

        return suspiciousIpRepository.save(address);
    }

    @Override
    @Transactional
    public AddressDeletionResponse removeIP(String ip) throws AddressNotFoundException {

        if (AntiFraudUtil.isValidIP().negate().test(ip)) {
            throw new InvalidIpException("IP address not valid.");
        }

        Address address = suspiciousIpRepository.findByIp(ip).orElseThrow(() -> new AddressNotFoundException("IP address not found!"));
        suspiciousIpRepository.delete(address);

        String status = String.format("IP %s successfully removed!", ip);

        return AddressDeletionResponse.builder().status(status).build();
    }

    @Override
    public boolean isBlacklistedIp(String ip) {
        return this.suspiciousIpRepository.findByIp(ip).isPresent();
    }
}
