package io.payguard.userservice.application.merchant.payment;

import io.payguard.userservice.domain.merchant.Merchant;

public record PreparedPaymentAccountCreation(

        Merchant merchant,
        boolean accountCreationRequired

) {
}
