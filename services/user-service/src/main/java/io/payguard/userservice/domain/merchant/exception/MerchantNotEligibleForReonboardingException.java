package io.payguard.userservice.domain.merchant.exception;

public class MerchantNotEligibleForReonboardingException
        extends MerchantException {

    public MerchantNotEligibleForReonboardingException() {
        super(
                "Merchant is not eligible for payment account re-onboarding."
        );
    }
}