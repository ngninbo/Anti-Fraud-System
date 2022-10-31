package antifraud.rest;

import lombok.*;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class UserDeletionResponse {

    private String username;
    private String status;

    public static final String DEFAULT_STATUS = "Deleted successfully!";
}
