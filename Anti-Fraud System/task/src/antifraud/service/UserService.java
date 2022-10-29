package antifraud.service;

import antifraud.domain.*;
import antifraud.exception.AdminLockException;
import antifraud.exception.RoleUpdateException;
import antifraud.exception.UserAlreadyExistException;
import antifraud.exception.UserNotFoundException;
import antifraud.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    UserDto create(User user) throws UserAlreadyExistException;

    Optional<User> findByUsername(String name);

    List<UserDto> findAll();

    UserDeletionResponse remove(String username) throws UserNotFoundException;

    UserDto update(RoleChangeRequest request) throws UserNotFoundException, RoleUpdateException, UserAlreadyExistException;

    AccessUpdateResponse updateAccess(AccessUpdateRequest request) throws UserNotFoundException, AdminLockException;
}
