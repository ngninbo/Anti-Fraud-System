package antifraud.controller;

import antifraud.domain.AntiFraudCustomErrorMessage;
import antifraud.rest.FeedbackUpdateRequest;
import antifraud.domain.TransactionDto;
import antifraud.rest.TransactionResponse;
import antifraud.model.Transaction;
import antifraud.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Transaction service", description = "Manage transactions")
public class TransactionController {

    public final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transaction")
    @Operation(description = "Save transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "400",
                    description = "Any validation exception",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access denied",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
    })
    public ResponseEntity<TransactionResponse> process(@Valid @RequestBody TransactionDto transactionDto) {
        return ResponseEntity.ok(transactionService.validate(transactionDto));
    }

    @PutMapping("/transaction")
    @Operation(description = "Set feedback of transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "400",
                    description = "Any validation exception",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access denied",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
    })
    public ResponseEntity<Transaction> setFeedback(@Valid @RequestBody FeedbackUpdateRequest request) {
        return ResponseEntity.ok(transactionService.updateTransactionFeedback(request));
    }

    @GetMapping("/history")
    @Operation(description = "Get transaction history")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access denied",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
    })
    public ResponseEntity<List<Transaction>> getHistory() {
        return ResponseEntity.ok(transactionService.findAll());
    }

    @GetMapping("/history/{number}")
    @Operation(description = "Find transaction history for card with given number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "400",
                    description = "card number validation failed",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access denied",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
            @ApiResponse(
                    responseCode = "404",
                    description = "No transaction for given number found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
    })
    public ResponseEntity<List<Transaction>> findByNumber(@PathVariable String number) {
        return ResponseEntity.ok(transactionService.findAllByNumber(number));
    }
}
