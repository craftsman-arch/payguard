package io.payguard.userservice.domain.settlement.value;

import io.payguard.userservice.domain.settlement.exception.InvalidAccountHolderNameException;

public record AccountHolderName(String value) {

    private static final int MAX_LENGTH = 255;

    public AccountHolderName {
        if (value == null || value.isBlank()) {
            throw new InvalidAccountHolderNameException();
        }

        value = value.trim();

        if (value.length() > MAX_LENGTH) {
            throw new InvalidAccountHolderNameException();
        }
    }

    public static AccountHolderName of(String value) {
        return new AccountHolderName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
