package antifraud.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class FeedbackUpdateRequest {

    @NotNull
    private Long transactionId;
    @NotEmpty
    private String feedback;
}
