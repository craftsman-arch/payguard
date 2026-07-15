package io.payguard.userservice.application.payment;

import io.payguard.userservice.domain.merchant.Merchant;

public interface PaymentProvider {

    PaymentAccount createAccount(Merchant merchant);

}