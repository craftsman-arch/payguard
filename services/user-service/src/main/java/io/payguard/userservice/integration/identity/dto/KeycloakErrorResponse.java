package io.payguard.userservice.integration.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public record KeycloakErrorResponse(

        String error,
        @JsonProperty("error_description")
        String errorDescription

) {

    private static final Set<String> PASSWORD_POLICY_ERRORS = Set.of(
            "invalidPasswordMinLengthMessage",
            "invalidPasswordMaxLengthMessage",
            "invalidPasswordNotUsernameMessage",
            "invalidPasswordNotEmailMessage",
            "invalidPasswordHistoryMessage"
    );

    public boolean isPasswordPolicyViolation() {
        return PASSWORD_POLICY_ERRORS.contains(error);
    }
}
