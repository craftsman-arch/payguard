package io.payguard.userservice.application.merchant.register;

import io.payguard.userservice.domain.merchant.BusinessType;
import io.payguard.userservice.domain.merchant.value.Country;
import io.payguard.userservice.domain.merchant.value.Email;

public record RegisterMerchantCommand(

        Email email,
        String password,
        String legalName,
        BusinessType businessType,
        Country country

) {

    @Override
    public String toString() {

        return "RegisterMerchantCommand[" +
                "email=" + email +
                ", password=[REDACTED]" +
                ", legalName=" + legalName +
                ", businessType=" + businessType +
                ", country=" + country +
                ']';
    }
}
