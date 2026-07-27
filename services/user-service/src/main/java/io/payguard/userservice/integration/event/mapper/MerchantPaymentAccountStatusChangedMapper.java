package io.payguard.userservice.integration.event.mapper;

import io.payguard.userservice.domain.merchant.MerchantPaymentState;
import io.payguard.userservice.domain.merchant.PaymentAccountRequiredAction;
import io.payguard.userservice.domain.merchant.PaymentAccountStatus;
import io.payguard.userservice.integration.event.contracts.merchant.v1.MerchantPaymentAccountStatusChanged;
import org.springframework.stereotype.Component;

@Component
public class MerchantPaymentAccountStatusChangedMapper {

    public MerchantPaymentAccountStatusChanged.PaymentState toContract(MerchantPaymentState state) {

        return new MerchantPaymentAccountStatusChanged.PaymentState(
                toContract(state.paymentAccountStatus()),
                toContract(state.requiredAction()),
                state.readyForPayments(),
                state.eligibleForDestinationCharges()
        );
    }

    private MerchantPaymentAccountStatusChanged.PaymentAccountStatus toContract(PaymentAccountStatus status) {

        return switch (status) {

            case PENDING_ONBOARDING ->
                    MerchantPaymentAccountStatusChanged
                            .PaymentAccountStatus
                            .PENDING_ONBOARDING;

            case ACTIVE ->
                    MerchantPaymentAccountStatusChanged
                            .PaymentAccountStatus
                            .ACTIVE;

            case RESTRICTED ->
                    MerchantPaymentAccountStatusChanged
                            .PaymentAccountStatus
                            .RESTRICTED;

            case DISABLED ->
                    MerchantPaymentAccountStatusChanged
                            .PaymentAccountStatus
                            .DISABLED;
        };
    }

    private MerchantPaymentAccountStatusChanged.PaymentAccountRequiredAction toContract(PaymentAccountRequiredAction requiredAction) {

        return switch (requiredAction) {

            case CONTINUE_ONBOARDING ->
                    MerchantPaymentAccountStatusChanged
                            .PaymentAccountRequiredAction
                            .CONTINUE_ONBOARDING;

            case WAIT_FOR_REVIEW ->
                    MerchantPaymentAccountStatusChanged
                            .PaymentAccountRequiredAction
                            .WAIT_FOR_REVIEW;

            case CONTACT_SUPPORT ->
                    MerchantPaymentAccountStatusChanged
                            .PaymentAccountRequiredAction
                            .CONTACT_SUPPORT;

            case NONE ->
                    MerchantPaymentAccountStatusChanged
                            .PaymentAccountRequiredAction
                            .NONE;
        };
    }
}
