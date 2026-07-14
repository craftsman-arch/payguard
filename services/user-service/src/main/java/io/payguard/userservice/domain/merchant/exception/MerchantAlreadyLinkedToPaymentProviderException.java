package io.payguard.userservice.domain.merchant.exception;

public class MerchantAlreadyLinkedToPaymentProviderException extends MerchantException {

    public MerchantAlreadyLinkedToPaymentProviderException() {
        super("Merchant is already linked to a payment provider.");
    }

}
