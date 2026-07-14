package io.payguard.userservice.domain.merchant.exception;

public class InvalidEmailException extends MerchantException {

    public InvalidEmailException() {
        super("Email cannot be null or blank.");
    }

    public InvalidEmailException(String email) {
        super("Invalid email address: " + email);
    }

}