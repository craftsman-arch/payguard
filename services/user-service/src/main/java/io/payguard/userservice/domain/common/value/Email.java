package io.payguard.userservice.domain.common.value;

import io.payguard.userservice.domain.common.exception.InvalidEmailException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.regex.Pattern;

@Getter
@EqualsAndHashCode
public final class Email {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email of(String value) {

        if (value == null || value.isBlank()) {
            throw new InvalidEmailException();
        }

        String normalized = value.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new InvalidEmailException(normalized);
        }

        return new Email(normalized);
    }

    @Override
    public String toString() {
        return value;
    }

}
