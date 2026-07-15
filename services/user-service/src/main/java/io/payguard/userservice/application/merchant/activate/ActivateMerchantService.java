package io.payguard.userservice.application.merchant.activate;

import io.payguard.userservice.application.time.TimeProvider;
import io.payguard.userservice.domain.merchant.Merchant;
import io.payguard.userservice.domain.merchant.MerchantRepository;
import io.payguard.userservice.domain.merchant.exception.MerchantNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ActivateMerchantService {

    private final MerchantRepository merchantRepository;
    private final TimeProvider timeProvider;

    public ActivateMerchantResult execute(ActivateMerchantCommand command) {

        Merchant merchant = merchantRepository.findById(command.merchantId())
                .orElseThrow(() -> new MerchantNotFoundException(command.merchantId()));

        merchant.activate("", timeProvider.now());

        merchantRepository.update(merchant);

        return new ActivateMerchantResult(
                merchant.getId()
        );
    }

}