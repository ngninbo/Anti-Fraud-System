package antifraud.controller;

import antifraud.rest.FeedbackUpdateRequest;
import antifraud.domain.TransactionDto;
import antifraud.rest.TransactionResponse;
import antifraud.exception.*;
import antifraud.model.Transaction;
import antifraud.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/api/antifraud", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class TransactionController {

    public final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transaction")
    public ResponseEntity<TransactionResponse> process(@Valid @RequestBody TransactionDto transactionDto)
            throws UserNotFoundException, InvalidRegionException, TransactionDateParsingException {
        return ResponseEntity.ok(transactionService.validate(transactionDto));
    }

    @PutMapping("/transaction")
    public ResponseEntity<Transaction> setFeedback(@Valid @RequestBody FeedbackUpdateRequest request)
            throws TransactionNotFoundException, IllegalFeedbackException, TransactionFeedbackAlreadyExistException,
            TransactionFeedbackUpdateException, CardNotFoundException {
        return ResponseEntity.ok(transactionService.updateTransactionFeedback(request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<Transaction>> getHistory() {
        return ResponseEntity.ok(transactionService.findAll());
    }

    @GetMapping("/history/{number}")
    public ResponseEntity<List<Transaction>> findByNumber(@PathVariable String number) throws InvalidNumberException,
            TransactionNotFoundException {
        return ResponseEntity.ok(transactionService.findAllByNumber(number));
    }
}
