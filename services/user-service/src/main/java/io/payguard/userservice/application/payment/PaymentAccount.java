package io.payguard.userservice.application.payment;

public record PaymentAccount(

        String accountId,
        String onboardingUrl
) {
}