package io.payguard.userservice.domain.common.exception;

public class InvalidCountryException extends DomainValueException {

    public InvalidCountryException() {
        super("Country cannot be null or blank.");
    }

    public InvalidCountryException(String country) {
        super("Invalid ISO 3166-1 alpha-2 country code: " + country);
    }

}
