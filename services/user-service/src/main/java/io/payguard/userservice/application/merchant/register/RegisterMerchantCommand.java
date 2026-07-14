package io.payguard.userservice.application.merchant.register;

import io.payguard.userservice.domain.merchant.BusinessType;
import io.payguard.userservice.domain.merchant.value.Country;
import io.payguard.userservice.domain.merchant.value.Email;

public record RegisterMerchantCommand(

        Email email,

        String legalName,

        BusinessType businessType,

        Country country

) {
}