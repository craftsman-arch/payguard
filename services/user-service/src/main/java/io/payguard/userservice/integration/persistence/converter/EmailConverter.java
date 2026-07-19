package io.payguard.userservice.integration.persistence.converter;

import io.payguard.userservice.domain.merchant.value.Country;
import io.payguard.userservice.domain.merchant.value.Email;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import static java.util.Optional.ofNullable;

@Converter(autoApply = true)
public class EmailConverter implements AttributeConverter<Email, String> {

    @Override
    public String convertToDatabaseColumn(Email email) {
        return ofNullable(email)
                .map(Email::getValue)
                .orElse(null);
    }

    @Override
    public Email convertToEntityAttribute(String value) {
        return ofNullable(value)
                .map(Email::of)
                .orElse(null);
    }

}