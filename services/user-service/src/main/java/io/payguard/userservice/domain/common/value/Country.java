package io.payguard.userservice.domain.common.value;

import io.payguard.userservice.domain.common.exception.InvalidCountryException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Locale;
import java.util.Set;

@Getter
@EqualsAndHashCode
public final class Country {

    private static final Set<String> ISO_COUNTRIES = Set.of(Locale.getISOCountries());

    private final String value;

    private Country(String value) {
        this.value = value;
    }

    public static Country of(String value) {

        if (value == null || value.isBlank()) {
            throw new InvalidCountryException();
        }

        String normalized = value.trim().toUpperCase();

        if (!ISO_COUNTRIES.contains(normalized)) {
            throw new InvalidCountryException(normalized);
        }

        return new Country(normalized);
    }

    @Override
    public String toString() {
        return value;
    }

}
