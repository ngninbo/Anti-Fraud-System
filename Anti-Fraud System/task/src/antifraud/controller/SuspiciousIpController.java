package antifraud.controller;

import antifraud.rest.AddressDeletionResponse;
import antifraud.exception.AddressAlreadyExistException;
import antifraud.exception.AddressNotFoundException;
import antifraud.model.Address;
import antifraud.service.SuspiciousIpService;
import antifraud.util.AntiFraudUtil;
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
public class SuspiciousIpController {

    private final SuspiciousIpService suspiciousIpService;

    @Autowired
    public SuspiciousIpController(SuspiciousIpService suspiciousIpService) {
        this.suspiciousIpService = suspiciousIpService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Address> create(@Valid @RequestBody Address address) throws AddressAlreadyExistException {
        return ResponseEntity.ok(suspiciousIpService.create(address));
    }

    @DeleteMapping("/{ip}")
    public ResponseEntity<AddressDeletionResponse> delete(@PathVariable String ip) throws AddressNotFoundException {
        if (AntiFraudUtil.isValidIP().negate().test(ip)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(suspiciousIpService.removeIP(ip));
    }

    @GetMapping
    public ResponseEntity<List<Address>> fetch() {
        return ResponseEntity.ok(suspiciousIpService.findAll());
    }
}
