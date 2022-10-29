package antifraud.controller;

import antifraud.domain.CardDeletionResponse;
import antifraud.exception.CardAlreadyExistException;
import antifraud.exception.CardNotFoundException;
import antifraud.model.Card;
import antifraud.service.CardBlacklistService;
import antifraud.util.AntiFraudValidator;
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
public class CardController {

    private final CardBlacklistService cardBlacklistService;

    @Autowired
    public CardController(CardBlacklistService cardBlacklistService) {
        this.cardBlacklistService = cardBlacklistService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Card> create(@Valid @RequestBody Card card) throws CardAlreadyExistException {
        return ResponseEntity.ok(cardBlacklistService.create(card));
    }

    @DeleteMapping("/{number}")
    public ResponseEntity<CardDeletionResponse> delete(@PathVariable String number) throws CardNotFoundException {
        if (AntiFraudValidator.isValidNumber().negate().test(number)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(cardBlacklistService.removeByNumber(number));
    }

    @GetMapping
    public ResponseEntity<List<Card>> fetch() {
        return ResponseEntity.ok(cardBlacklistService.findAll());
    }
}
