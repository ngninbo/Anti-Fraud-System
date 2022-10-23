package antifraud.controller;

import antifraud.domain.UserDeletionResponse;
import antifraud.domain.UserDto;
import antifraud.exception.UserAlreadyExistException;
import antifraud.exception.UserNotFoundException;
import antifraud.model.User;
import antifraud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class UserController {

    private final UserService userService;
    private final PasswordEncoder encoder;

    @Autowired
    public UserController(UserService userService, PasswordEncoder encoder) {
        this.userService = userService;
        this.encoder = encoder;
    }

    @PostMapping("/auth/user")
    public ResponseEntity<UserDto> create(@Valid @RequestBody User user) throws UserAlreadyExistException {
        user.setPassword(encoder.encode(user.getPassword()));
        return new ResponseEntity<>(userService.create(user), HttpStatus.CREATED);
    }

    @GetMapping("auth/list")
    public ResponseEntity<List<UserDto>> fetchAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @DeleteMapping("/auth/user/{username}")
    public ResponseEntity<UserDeletionResponse> remove(@PathVariable String username) throws UserNotFoundException {
        return ResponseEntity.ok(userService.remove(username));
    }
}
