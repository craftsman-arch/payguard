package io.payguard.userservice.application.payment;

import io.payguard.userservice.domain.merchant.Merchant;

public interface PaymentProvider {

    String createMerchantAccount(Merchant merchant);

    String createMerchantOnboardingLink(Merchant merchant);

}
