package antifraud.domain;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class AntiFraudCustomErrorMessage {

    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
