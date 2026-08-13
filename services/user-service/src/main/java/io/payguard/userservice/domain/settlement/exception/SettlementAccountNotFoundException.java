package io.payguard.userservice.domain.settlement.exception;

import io.payguard.userservice.domain.user.value.UserId;

public class SettlementAccountNotFoundException
        extends SettlementAccountException {

    public SettlementAccountNotFoundException(UserId userId) {
        super(
                "Settlement account for user [%s] was not found."
                        .formatted(userId.value())
        );
    }
}
