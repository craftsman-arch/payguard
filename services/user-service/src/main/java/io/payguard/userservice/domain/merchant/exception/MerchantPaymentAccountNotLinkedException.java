package io.payguard.userservice.domain.merchant.exception;

public class MerchantPaymentAccountNotLinkedException extends MerchantException {

    private static final String MESSAGE =
            "Merchant is not linked to a payment provider.";

    public MerchantPaymentAccountNotLinkedException() {
        super(MESSAGE);
    }

}