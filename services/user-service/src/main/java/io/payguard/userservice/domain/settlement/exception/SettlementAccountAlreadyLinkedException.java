package io.payguard.userservice.domain.settlement.exception;

public class SettlementAccountAlreadyLinkedException
        extends SettlementAccountException {

    public SettlementAccountAlreadyLinkedException() {
        super("Settlement account is already linked to a provider account.");
    }
}
