package antifraud.controller;

import antifraud.domain.CardDeletionResponse;
import antifraud.exception.CardAlreadyExistException;
import antifraud.exception.CardNotFoundException;
import antifraud.model.Card;
import antifraud.service.CardService;
import antifraud.util.AntiFraudValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    private final CardService cardService;

    @Autowired
    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Card> create(@Valid @RequestBody Card card) throws CardAlreadyExistException {
        return ResponseEntity.ok(cardService.create(card));
    }

    @DeleteMapping("/{number}")
    public ResponseEntity<CardDeletionResponse> delete(@PathVariable String number) throws CardNotFoundException {
        if (AntiFraudValidator.isValidNumber().negate().test(number)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(cardService.removeByNumber(number));
    }

    @GetMapping
    public ResponseEntity<List<Card>> fetch() {
        return ResponseEntity.ok(cardService.findAll());
    }
}
