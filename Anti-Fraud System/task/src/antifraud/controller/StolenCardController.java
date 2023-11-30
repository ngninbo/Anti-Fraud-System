package antifraud.controller;

import antifraud.domain.AntiFraudCustomErrorMessage;
import antifraud.model.StolenCard;
import antifraud.rest.CardDeletionResponse;
import antifraud.service.StolenCardService;
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
@RequestMapping(path = "/api/antifraud/stolencard", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Tag(name = "Stolen cards service", description = "manage stolen cards")
public class StolenCardController {

    private final StolenCardService stolenCardService;

    @Autowired
    public StolenCardController(StolenCardService stolenCardService) {
        this.stolenCardService = stolenCardService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Add stolen card")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "409",
                    description = "Card already exist!",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access denied",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
    })
    public ResponseEntity<StolenCard> create(@Valid @RequestBody StolenCard stolenCard) {
        return ResponseEntity.ok(stolenCardService.create(stolenCard));
    }

    @DeleteMapping("/{number}")
    @Operation(description = "Delete stolen card by number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation of given card number with Luhn Algorithm failed.",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access denied",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
            @ApiResponse(responseCode = "404",
                    description = "Card number not found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) })
    })
    public ResponseEntity<CardDeletionResponse> delete(@PathVariable String number) {
        return ResponseEntity.ok(stolenCardService.removeByNumber(number));
    }

    @GetMapping
    @Operation(description = "Get list of all stolen cards")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access denied",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
    })
    public ResponseEntity<List<StolenCard>> fetch() {
        return ResponseEntity.ok(stolenCardService.findAll());
    }
}
