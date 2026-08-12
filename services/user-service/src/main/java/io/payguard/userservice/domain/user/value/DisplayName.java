package io.payguard.userservice.domain.user.value;

import io.payguard.userservice.domain.user.exception.InvalidDisplayNameException;

public record DisplayName(String value) {

    private static final int MAX_LENGTH = 100;

    public DisplayName {

        if (value == null || value.isBlank()) {
            throw new InvalidDisplayNameException();
        }

        value = value.trim();

        if (value.length() > MAX_LENGTH) {
            throw new InvalidDisplayNameException();
        }
    }

    public static DisplayName of(String value) {
        return new DisplayName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
