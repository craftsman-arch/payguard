package io.payguard.userservice.application.payment;

public interface PaymentProvider {

    String createSettlementAccount(
            SettlementAccountProvisioningRequest request
    );

    String createSettlementAccountOnboardingLink(
            SettlementAccountOnboardingRequest request
    );

}
