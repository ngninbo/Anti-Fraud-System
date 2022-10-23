package antifraud.domain;

import lombok.*;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String name;
    private String username;
    private String role;
}
