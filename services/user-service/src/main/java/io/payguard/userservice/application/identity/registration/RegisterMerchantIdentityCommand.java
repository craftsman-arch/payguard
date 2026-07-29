package io.payguard.userservice.application.identity.registration;

import io.payguard.userservice.domain.merchant.value.Email;

public record RegisterMerchantIdentityCommand(Email email, String password) {

    @Override
    public String toString() {

        return "RegisterMerchantIdentityCommand[" +
                "email=" + email +
                ", password=[REDACTED]" +
                ']';
    }
}
