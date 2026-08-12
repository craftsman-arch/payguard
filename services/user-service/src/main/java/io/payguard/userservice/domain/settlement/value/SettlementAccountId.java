package io.payguard.userservice.domain.settlement.value;

import lombok.NonNull;

import java.util.UUID;

public record SettlementAccountId(@NonNull UUID value) {

    public static SettlementAccountId generate() {
        return new SettlementAccountId(UUID.randomUUID());
    }

    public static SettlementAccountId of(UUID value) {
        return new SettlementAccountId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
