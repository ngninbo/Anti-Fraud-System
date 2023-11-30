package antifraud.controller;

import antifraud.domain.*;
import antifraud.exception.AdminLockException;
import antifraud.exception.UserNotFoundException;
import antifraud.model.User;
import antifraud.rest.AccessUpdateRequest;
import antifraud.rest.AccessUpdateResponse;
import antifraud.rest.RoleChangeRequest;
import antifraud.rest.UserDeletionResponse;
import antifraud.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@AllArgsConstructor
@Tag(name = "Auth service", description = "manage users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder encoder;

    @PostMapping("/user")
    @Operation(description = "Create user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "400",
                    description = "Any validation exception",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) })
    })
    public ResponseEntity<UserDto> create(@Valid @RequestBody User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return new ResponseEntity<>(userService.create(user), HttpStatus.CREATED);
    }

    @GetMapping("/list")
    @Operation(description = "Get list of users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access denied",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) })
    })
    public ResponseEntity<List<UserDto>> fetchAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @DeleteMapping("/user/{username}")
    @Operation(description = "Delete user by given email")
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
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) })
    })
    public ResponseEntity<UserDeletionResponse> remove(@PathVariable String username) {
        return ResponseEntity.ok(userService.remove(username));
    }

    @PutMapping("/role")
    @Operation(description = "Change user role")
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
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
            @ApiResponse(
                    responseCode = "409",
                    description = "User already has the role",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) })
    })
    public ResponseEntity<UserDto> changeRole(@Valid @RequestBody RoleChangeRequest request) {
        return ResponseEntity.ok(userService.update(request));
    }

    @PutMapping("/access")
    @Operation(description = "Update user access")
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
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) }),
            @ApiResponse(
                    responseCode = "409",
                    description = "User already has the role",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AntiFraudCustomErrorMessage.class)) })
    })
    public ResponseEntity<AccessUpdateResponse> updateAccess(@Valid @RequestBody AccessUpdateRequest request)
            throws UserNotFoundException, AdminLockException {
        return ResponseEntity.ok(userService.updateAccess(request));
    }
}
