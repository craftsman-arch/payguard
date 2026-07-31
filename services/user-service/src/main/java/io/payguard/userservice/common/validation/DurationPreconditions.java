package io.payguard.userservice.common.validation;

import java.time.Duration;

import static java.util.Objects.requireNonNull;

public final class DurationPreconditions {

    private DurationPreconditions() {
    }

    public static void requirePositive(Duration value, String message) {

        requireNonNull(value, message);

        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void requireNotNegative(Duration value, String message) {

        requireNonNull(value, message);

        if (value.isNegative()) {
            throw new IllegalArgumentException(message);
        }
    }
}
