package io.payguard.userservice.domain.settlement.exception;

public class InvalidAccountHolderNameException extends SettlementAccountException {

    public InvalidAccountHolderNameException() {
        super("Account holder name must contain between 1 and 255 characters.");
    }
}
