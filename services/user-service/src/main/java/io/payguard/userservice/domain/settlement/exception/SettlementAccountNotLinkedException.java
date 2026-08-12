package io.payguard.userservice.domain.settlement.exception;

public class SettlementAccountNotLinkedException
        extends SettlementAccountException {

    public SettlementAccountNotLinkedException() {
        super("Settlement account is not linked to a provider account.");
    }
}
