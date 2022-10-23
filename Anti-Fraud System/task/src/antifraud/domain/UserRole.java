package antifraud.domain;

public enum UserRole {

    ROLE_MERCHANT("MERCHANT"),
    ROLE_ADMINISTRATOR("ADMINISTRATOR"),
    ROLE_SUPPORT("SUPPORT");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
