package io.payguard.userservice.application.merchant.profile;

import io.payguard.userservice.domain.merchant.BusinessType;
import io.payguard.userservice.domain.common.value.Country;
import lombok.NonNull;

public record CreateMerchantProfileCommand(

        @NonNull String legalName,
        @NonNull BusinessType businessType,
        @NonNull Country country

) {
}
