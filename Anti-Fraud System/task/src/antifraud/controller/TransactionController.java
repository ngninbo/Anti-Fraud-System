package antifraud.controller;

import antifraud.domain.TransactionResponse;
import antifraud.model.Transaction;
import antifraud.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

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
    public ResponseEntity<TransactionResponse> process(@Valid @RequestBody Transaction transaction) {
        return ResponseEntity.ok(transactionService.validate(transaction));
    }
}
