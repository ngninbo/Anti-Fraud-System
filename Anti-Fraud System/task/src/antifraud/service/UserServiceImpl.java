package antifraud.service;

import antifraud.domain.*;
import antifraud.exception.AdminLockException;
import antifraud.exception.RoleUpdateException;
import antifraud.exception.UserAlreadyExistException;
import antifraud.exception.UserNotFoundException;
import antifraud.mapper.UserMapper;
import antifraud.model.User;
import antifraud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static antifraud.domain.UserRole.*;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserDto create(User user) throws UserAlreadyExistException {

        var count = userRepository.count();

        if (count <= 0) {
            user.setAccountNonLocked(true);
            user.setRole(ROLE_ADMINISTRATOR);
        } else {
            var userFromRepo = userRepository.findUsersByUsernameIgnoreCase(user.getUsername());

            if (userFromRepo.isPresent()) {
                throw new UserAlreadyExistException("User already exist!");
            }

            user.setRole(ROLE_MERCHANT);

        }

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public Optional<User> findByUsername(String name) throws UsernameNotFoundException {
        return userRepository.findUsersByUsernameIgnoreCase(name);
    }

    @Override
    public List<UserDto> findAll() {
        return userMapper.toList(userRepository.findAll());
    }

    @Override
    @Transactional
    public UserDeletionResponse remove(String username) throws UserNotFoundException {
        User user = userRepository.findUsersByUsernameIgnoreCase(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        userRepository.delete(user);
        return UserDeletionResponse.builder().status(UserDeletionResponse.DEFAULT_STATUS).username(username).build();
    }

    @Override
    @Transactional
    public UserDto update(RoleChangeRequest request) throws UserNotFoundException, RoleUpdateException, UserAlreadyExistException {

        User user = userRepository.findUsersByUsernameIgnoreCase(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        if (user.isAdmin()) {
            throw new RoleUpdateException("User can have only one role");
        }

        if (user.getRole().getDescription().equals(request.getRole())) {
            throw new UserAlreadyExistException("User already has the role");
        }

        user.setRole(UserRole.valueOf("ROLE_" + request.getRole()));
        user = userRepository.save(user);

        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public AccessUpdateResponse updateAccess(AccessUpdateRequest request) throws UserNotFoundException, AdminLockException {

        User user = userRepository.findUsersByUsernameIgnoreCase(request.getUsername()).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.isAdmin()) {
            throw new AdminLockException(String.format("%s cannot be blocked", ROLE_ADMINISTRATOR.getDescription()));
        }

        final String operation = request.getOperation();
        user.setAccountNonLocked(!"LOCK".equals(operation));

        user = userRepository.save(user);

        return AccessUpdateResponse.builder()
                .status(String.format("User %s %sed!", user.getUsername(), operation.toLowerCase()))
                .build();
    }
}
