package io.payguard.userservice.domain.settlement.exception;

public class InvalidSettlementAccountRevisionException
        extends SettlementAccountException {

    public InvalidSettlementAccountRevisionException(long revision) {
        super("Settlement account revision must not be negative: " + revision);
    }
}
