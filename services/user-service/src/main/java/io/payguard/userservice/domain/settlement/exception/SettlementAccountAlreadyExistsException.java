package io.payguard.userservice.domain.settlement.exception;

import io.payguard.userservice.domain.user.value.UserId;

public class SettlementAccountAlreadyExistsException extends SettlementAccountException {

    public SettlementAccountAlreadyExistsException(UserId userId, Throwable cause) {
        super(
                "Settlement account for user [%s] already exists."
                        .formatted(userId.value()),
                cause
        );
    }
}
