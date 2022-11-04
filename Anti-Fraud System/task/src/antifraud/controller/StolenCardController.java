package antifraud.controller;

import antifraud.model.StolenCard;
import antifraud.rest.CardDeletionResponse;
import antifraud.service.StolenCardService;
import antifraud.util.AntiFraudUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/api/antifraud/stolencard", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class StolenCardController {

    private final StolenCardService stolenCardService;

    @Autowired
    public StolenCardController(StolenCardService stolenCardService) {
        this.stolenCardService = stolenCardService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StolenCard> create(@Valid @RequestBody StolenCard stolenCard) {
        return ResponseEntity.ok(stolenCardService.create(stolenCard));
    }

    @DeleteMapping("/{number}")
    public ResponseEntity<CardDeletionResponse> delete(@PathVariable String number) {
        if (AntiFraudUtil.isValidNumber().negate().test(number)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(stolenCardService.removeByNumber(number));
    }

    @GetMapping
    public ResponseEntity<List<StolenCard>> fetch() {
        return ResponseEntity.ok(stolenCardService.findAll());
    }
}
