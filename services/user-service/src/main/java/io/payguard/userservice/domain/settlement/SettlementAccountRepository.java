package io.payguard.userservice.domain.settlement;

import io.payguard.userservice.domain.settlement.value.SettlementAccountId;
import io.payguard.userservice.domain.user.value.UserId;

import java.util.Optional;

public interface SettlementAccountRepository {

    void add(SettlementAccount settlementAccount);

    void update(SettlementAccount settlementAccount);

    Optional<SettlementAccount> findById(SettlementAccountId id);

    Optional<SettlementAccount> findByUserId(UserId userId);

    Optional<SettlementAccount> findByProviderAccountId(
            SettlementProvider provider,
            String providerAccountId
    );
}
