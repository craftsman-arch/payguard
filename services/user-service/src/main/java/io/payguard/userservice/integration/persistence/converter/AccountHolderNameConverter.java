package io.payguard.userservice.integration.persistence.converter;

import io.payguard.userservice.domain.settlement.value.AccountHolderName;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import static java.util.Optional.ofNullable;

@Converter(autoApply = true)
public class AccountHolderNameConverter implements AttributeConverter<AccountHolderName, String> {

    @Override
    public String convertToDatabaseColumn(AccountHolderName name) {
        return ofNullable(name)
                .map(AccountHolderName::value)
                .orElse(null);
    }

    @Override
    public AccountHolderName convertToEntityAttribute(String value) {
        return ofNullable(value)
                .map(AccountHolderName::of)
                .orElse(null);
    }
}
