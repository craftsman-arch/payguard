package io.payguard.userservice.application.merchant.payment;

import io.payguard.userservice.domain.merchant.Merchant;
import io.payguard.userservice.domain.merchant.MerchantRepository;
import io.payguard.userservice.domain.merchant.exception.MerchantNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantPaymentContextQueryService {

    private final MerchantRepository merchantRepository;

    public MerchantPaymentContext execute(UUID merchantId) {

        Merchant merchant = merchantRepository
                .findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(
                        "Merchant with id '%s' was not found."
                                .formatted(merchantId)
                ));

        return new MerchantPaymentContext(
                merchant.getId(),
                merchant.getPaymentAccountId(),
                merchant.getPaymentAccountStatus(),
                merchant.isReadyForPayments(),
                merchant.isEligibleForDestinationCharges()
        );
    }
}
