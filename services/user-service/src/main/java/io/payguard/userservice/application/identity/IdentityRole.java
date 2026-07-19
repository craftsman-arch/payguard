package io.payguard.userservice.application.identity;

public enum IdentityRole {

    MERCHANT("MERCHANT"),
    ADMIN("ADMIN"),
    SUPPORT("SUPPORT");

    private final String value;

    IdentityRole(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}