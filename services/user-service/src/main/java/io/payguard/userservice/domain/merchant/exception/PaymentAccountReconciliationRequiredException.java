package io.payguard.userservice.domain.merchant.exception;

import java.util.UUID;

public class PaymentAccountReconciliationRequiredException extends MerchantException {

    public PaymentAccountReconciliationRequiredException(UUID merchantId) {
        super(
                "Payment-account creation for merchant '%s' requires reconciliation."
                        .formatted(merchantId)
        );
    }
}
