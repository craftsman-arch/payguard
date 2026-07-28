package io.payguard.userservice.integration.identity.dto;

public record KeycloakCredential(

        String type,
        String value,
        boolean temporary
) {

    @Override
    public String toString() {

        return "KeycloakCredential[" +
                "type=" + type +
                ", value=[REDACTED]" +
                ", temporary=" + temporary +
                ']';
    }
}
