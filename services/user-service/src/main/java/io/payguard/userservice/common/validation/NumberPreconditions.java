package io.payguard.userservice.common.validation;

public final class NumberPreconditions {

    private NumberPreconditions() {
    }

    public static void requirePositive(int value, String message) {

        if (value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void requirePercentage(float value, String message) {

        if (value <= 0 || value > 100) {
            throw new IllegalArgumentException(message);
        }
    }
}
