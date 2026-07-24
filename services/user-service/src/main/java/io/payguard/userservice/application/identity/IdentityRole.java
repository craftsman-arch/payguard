package io.payguard.userservice.application.identity;

public enum IdentityRole {

    MERCHANT("MERCHANT"),
    ADMIN("ADMIN"),
    RISK_ANALYST("RISK_ANALYST");

    private final String value;

    IdentityRole(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
