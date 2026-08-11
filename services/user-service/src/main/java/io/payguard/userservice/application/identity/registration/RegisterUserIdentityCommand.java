package io.payguard.userservice.application.identity.registration;

import io.payguard.userservice.domain.merchant.value.Email;

public record RegisterUserIdentityCommand(Email email, String password) {

    @Override
    public String toString() {

        return "RegisterUserIdentityCommand[" +
                "email=" + email +
                ", password=[REDACTED]" +
                ']';
    }
}
