package antifraud.mapper;

import antifraud.domain.UserDto;
import antifraud.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        return UserDto.builder().id(user.getId()).name(user.getName())
                .username(user.getUsername()).role(user.getRole().getDescription()).build();
    }

    public List<UserDto> toList(List<User> users) {
        return users.isEmpty() ? List.of() : users.stream().map(this::toDto).collect(Collectors.toList());
    }
}
