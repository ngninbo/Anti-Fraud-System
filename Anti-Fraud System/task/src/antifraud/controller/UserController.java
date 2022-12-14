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
public class UserController {

    private final UserService userService;
    private final PasswordEncoder encoder;

    @PostMapping("/user")
    public ResponseEntity<UserDto> create(@Valid @RequestBody User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return new ResponseEntity<>(userService.create(user), HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserDto>> fetchAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @DeleteMapping("/user/{username}")
    public ResponseEntity<UserDeletionResponse> remove(@PathVariable String username) {
        return ResponseEntity.ok(userService.remove(username));
    }

    @PutMapping("/role")
    public ResponseEntity<UserDto> changeRole(@Valid @RequestBody RoleChangeRequest request) {
        return ResponseEntity.ok(userService.update(request));
    }

    @PutMapping("/access")
    public ResponseEntity<AccessUpdateResponse> updateAccess(@Valid @RequestBody AccessUpdateRequest request)
            throws UserNotFoundException, AdminLockException {
        return ResponseEntity.ok(userService.updateAccess(request));
    }
}
