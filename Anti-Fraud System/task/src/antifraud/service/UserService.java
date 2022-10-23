package antifraud.service;

import antifraud.domain.UserDeletionResponse;
import antifraud.domain.UserDto;
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

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserDto create(User user) throws UserAlreadyExistException {
        var userFromRepo = userRepository.findUsersByUsernameIgnoreCase(user.getUsername());

        if (userFromRepo.isPresent()) {
            throw new UserAlreadyExistException("User already exist!");
        }

        user.setRole("ROLE_USER");

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
}
