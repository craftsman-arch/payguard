package io.payguard.userservice.integration.persistence.converter;

import io.payguard.userservice.domain.merchant.value.Country;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import static java.util.Optional.ofNullable;

@Converter(autoApply = true)
public class CountryConverter implements AttributeConverter<Country, String> {

    @Override
    public String convertToDatabaseColumn(Country country) {
        return ofNullable(country)
                .map(Country::getValue)
                .orElse(null);
    }

    @Override
    public Country convertToEntityAttribute(String value) {
        return ofNullable(value)
                .map(Country::of)
                .orElse(null);
    }

}