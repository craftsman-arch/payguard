package io.payguard.userservice.application.user.profile;

import io.payguard.userservice.domain.common.value.Country;
import io.payguard.userservice.domain.settlement.SettlementAccountHolderType;
import io.payguard.userservice.domain.settlement.value.AccountHolderName;
import io.payguard.userservice.domain.user.value.DisplayName;
import lombok.NonNull;

public record CreateUserProfileCommand(
        @NonNull DisplayName displayName,
        @NonNull AccountHolderName accountHolderName,
        @NonNull SettlementAccountHolderType accountHolderType,
        @NonNull Country country
) {
}
