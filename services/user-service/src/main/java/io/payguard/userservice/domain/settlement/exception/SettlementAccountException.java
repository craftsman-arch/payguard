package io.payguard.userservice.domain.settlement.exception;

public abstract class SettlementAccountException extends RuntimeException {

    protected SettlementAccountException(String message) {
        super(message);
    }
}
