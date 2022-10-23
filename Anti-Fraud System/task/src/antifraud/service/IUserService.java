package antifraud.service;

import antifraud.domain.UserDeletionResponse;
import antifraud.domain.UserDto;
import antifraud.exception.UserAlreadyExistException;
import antifraud.exception.UserNotFoundException;
import antifraud.model.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {

    UserDto create(User user) throws UserAlreadyExistException;

    Optional<User> findByUsername(String name);

    List<UserDto> findAll();

    UserDeletionResponse remove(String username) throws UserNotFoundException;
}
