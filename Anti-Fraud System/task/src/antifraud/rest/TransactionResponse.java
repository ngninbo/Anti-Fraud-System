package antifraud.rest;

import antifraud.domain.TransactionValidationResult;
import lombok.*;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private TransactionValidationResult result;

    private String info;
}
