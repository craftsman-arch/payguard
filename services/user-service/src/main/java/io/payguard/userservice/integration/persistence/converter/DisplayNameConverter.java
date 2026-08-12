package io.payguard.userservice.integration.persistence.converter;

import io.payguard.userservice.domain.user.value.DisplayName;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import static java.util.Optional.ofNullable;

@Converter(autoApply = true)
public class DisplayNameConverter implements AttributeConverter<DisplayName, String> {

    @Override
    public String convertToDatabaseColumn(DisplayName displayName) {
        return ofNullable(displayName)
                .map(DisplayName::value)
                .orElse(null);
    }

    @Override
    public DisplayName convertToEntityAttribute(String value) {
        return ofNullable(value)
                .map(DisplayName::of)
                .orElse(null);
    }
}
