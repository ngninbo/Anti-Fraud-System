package antifraud.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;

import java.util.function.Predicate;

import static antifraud.domain.UserRole.*;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RoleChangeRequest {

    @NotEmpty(message = "username must not be empty")
    private String username;
    @NotEmpty(message = "role must not be empty")
    private String role;

    @AssertTrue(message = "Role must be SUPPORT or MERCHANT")
    public boolean isValidRole() {
        return isNotAdminRole().test(role);
    }

    private Predicate<String> isNotAdminRole() {
        return role -> ROLE_SUPPORT.getDescription().equals(role) || ROLE_MERCHANT.getDescription().equals(role);
    }
}
