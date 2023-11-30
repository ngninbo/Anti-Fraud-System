package antifraud.controller;

import antifraud.domain.AntiFraudCustomErrorMessage;
import antifraud.rest.AddressDeletionResponse;
import antifraud.model.Address;
import antifraud.service.SuspiciousIpService;
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
@RequestMapping(path = "/api/antifraud/suspicious-ip", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Tag(name = "Suspicious IP service", description = "manage suspicious ip address")
public class SuspiciousIpController {

    private final SuspiciousIpService suspiciousIpService;

    @Autowired
    public SuspiciousIpController(SuspiciousIpService suspiciousIpService) {
        this.suspiciousIpService = suspiciousIpService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Add new IP Address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "409",
                    description = "IP address already exist!",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access denied",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
    })
    public ResponseEntity<Address> create(@Valid @RequestBody Address address) {
        return ResponseEntity.ok(suspiciousIpService.create(address));
    }

    @DeleteMapping("/{ip}")
    @Operation(description = "Remove IP Address by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "400",
                    description = "IP address not valid.",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access denied",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
            @ApiResponse(
                    responseCode = "404",
                    description = "IP address not found!",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
    })
    public ResponseEntity<AddressDeletionResponse> delete(@PathVariable String ip) {
        return ResponseEntity.ok(suspiciousIpService.removeIP(ip));
    }

    @GetMapping
    @Operation(description = "Get all IP addresses")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access denied",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
    })
    public ResponseEntity<List<Address>> fetch() {
        return ResponseEntity.ok(suspiciousIpService.findAll());
    }
}
