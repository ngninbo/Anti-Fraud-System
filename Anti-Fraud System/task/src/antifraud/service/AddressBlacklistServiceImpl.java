package antifraud.service;

import antifraud.domain.AddressDeletionResponse;
import antifraud.exception.AddressAlreadyExistException;
import antifraud.exception.AddressNotFoundException;
import antifraud.model.Address;
import antifraud.repository.AddressBlacklistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class AddressBlacklistServiceImpl implements AddressBlacklistService {

    private final AddressBlacklistRepository addressBlacklistRepository;

    @Autowired
    public AddressBlacklistServiceImpl(AddressBlacklistRepository addressBlacklistRepository) {
        this.addressBlacklistRepository = addressBlacklistRepository;
    }

    @Override
    public List<Address> findAll() {
        return addressBlacklistRepository.findAll();
    }

    @Override
    @Transactional
    public Address create(Address address) throws AddressAlreadyExistException {

        var addressOptional = addressBlacklistRepository.findByIp(address.getIp());

        if (addressOptional.isPresent()) {
            throw new AddressAlreadyExistException("IP address already exist!");
        }

        return addressBlacklistRepository.save(address);
    }

    @Override
    @Transactional
    public AddressDeletionResponse removeIP(String ip) throws AddressNotFoundException {
        Address address = addressBlacklistRepository.findByIp(ip).orElseThrow(() -> new AddressNotFoundException("IP address not found!"));
        addressBlacklistRepository.delete(address);

        String status = String.format("IP %s successfully removed!", ip);

        return AddressDeletionResponse.builder().status(status).build();
    }

    @Override
    public boolean isBlacklistedIp(String ip) {
        return this.addressBlacklistRepository.findByIp(ip).isPresent();
    }
}
