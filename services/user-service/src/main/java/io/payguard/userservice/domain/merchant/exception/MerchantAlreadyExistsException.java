package io.payguard.userservice.domain.merchant.exception;

import io.payguard.userservice.domain.merchant.value.Email;

public class MerchantAlreadyExistsException extends MerchantException {

    public MerchantAlreadyExistsException(Email email) {
        super("Merchant with email '%s' already exists."
                .formatted(email.getValue()));
    }
}