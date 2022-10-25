package antifraud.domain;

import lombok.*;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private TransactionValidationResult result;

    private String info;
}
