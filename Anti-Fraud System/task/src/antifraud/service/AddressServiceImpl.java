package antifraud.service;

import antifraud.domain.AddressDeletionResponse;
import antifraud.exception.AddressAlreadyExistException;
import antifraud.exception.AddressNotFoundException;
import antifraud.model.Address;
import antifraud.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    @Autowired
    public AddressServiceImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public List<Address> findAll() {
        return addressRepository.findAll();
    }

    @Override
    @Transactional
    public Address create(Address address) throws AddressAlreadyExistException {

        var addressOptional = addressRepository.findByIp(address.getIp());

        if (addressOptional.isPresent()) {
            throw new AddressAlreadyExistException("IP address already exist!");
        }

        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public AddressDeletionResponse removeIP(String ip) throws AddressNotFoundException {
        Address address = addressRepository.findByIp(ip).orElseThrow(() -> new AddressNotFoundException("IP address not found!"));
        addressRepository.delete(address);

        String status = String.format("IP %s successfully removed!", ip);

        return AddressDeletionResponse.builder().status(status).build();
    }

    @Override
    public Optional<Address> findByIP(String ip) {
        return addressRepository.findByIp(ip);
    }
}
