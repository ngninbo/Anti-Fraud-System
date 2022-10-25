package antifraud.controller;

import antifraud.domain.AddressDeletionResponse;
import antifraud.exception.AddressAlreadyExistException;
import antifraud.exception.AddressNotFoundException;
import antifraud.model.Address;
import antifraud.service.AddressService;
import antifraud.util.AntiFraudValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/api/antifraud/suspicious-ip", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class AddressController {

    private final AddressService addressService;

    @Autowired
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Address> create(@Valid @RequestBody Address address) throws AddressAlreadyExistException {
        return ResponseEntity.ok(addressService.create(address));
    }

    @DeleteMapping("/{ip}")
    public ResponseEntity<AddressDeletionResponse> delete(@PathVariable String ip) throws AddressNotFoundException {
        if (AntiFraudValidator.isValidIP().negate().test(ip)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(addressService.removeIP(ip));
    }

    @GetMapping
    public ResponseEntity<List<Address>> fetch() {
        return ResponseEntity.ok(addressService.findAll());
    }
}
