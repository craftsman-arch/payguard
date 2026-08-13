package io.payguard.userservice.domain.settlement.exception;

import io.payguard.userservice.domain.settlement.value.SettlementAccountId;

public class ConcurrentSettlementAccountModificationException
        extends SettlementAccountException {

    public ConcurrentSettlementAccountModificationException(SettlementAccountId settlementAccountId) {
        super(
                "Settlement account [%s] was concurrently modified."
                        .formatted(settlementAccountId.value())
        );
    }
}
