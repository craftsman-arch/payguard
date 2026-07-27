package io.payguard.userservice.common.validation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextPreconditions {

    public static void requireText(String value, String message) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
