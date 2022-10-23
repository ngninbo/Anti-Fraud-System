package antifraud.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class AccessUpdateRequest {

    @NotEmpty(message = "username must not be mepty")
    private String username;
    @NotEmpty(message = "operation must not be empty")
    private String operation;

    @AssertTrue(message = "LOCK or UNLOCK operation supported")
    public boolean isValidOperation() {
        return "LOCK".equals(operation) || "UNLOCK".equals(operation);
    }
}
