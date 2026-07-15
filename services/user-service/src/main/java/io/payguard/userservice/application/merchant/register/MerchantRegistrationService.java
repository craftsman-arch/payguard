package io.payguard.userservice.application.merchant.register;

import io.payguard.userservice.application.id.IdGenerator;
import io.payguard.userservice.application.time.TimeProvider;
import io.payguard.userservice.domain.merchant.Merchant;
import io.payguard.userservice.domain.merchant.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MerchantRegistrationService {

    private final MerchantRepository merchantRepository;
    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;

    public Merchant register(RegisterMerchantCommand command) {

        return merchantRepository.findByEmail(command.email())
                .orElseGet(() -> createMerchant(command));
    }

    private Merchant createMerchant(RegisterMerchantCommand command) {

        Merchant merchant = Merchant.register(
                idGenerator.generate(),
                command.email(),
                command.legalName(),
                command.businessType(),
                command.country(),
                timeProvider.now()
        );

        merchantRepository.add(merchant);

        return merchant;
    }

}