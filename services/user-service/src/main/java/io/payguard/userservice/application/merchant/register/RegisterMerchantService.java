package io.payguard.userservice.application.merchant.register;

import io.payguard.userservice.application.id.IdGenerator;
import io.payguard.userservice.application.time.TimeProvider;
import io.payguard.userservice.domain.merchant.Merchant;
import io.payguard.userservice.domain.merchant.MerchantRepository;
import io.payguard.userservice.domain.merchant.exception.MerchantAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterMerchantService {

    private final MerchantRepository merchantRepository;
    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;

    public RegisterMerchantResult execute(RegisterMerchantCommand command) {

        if (merchantRepository.existsByEmail(command.email())) {
            throw new MerchantAlreadyExistsException(command.email());
        }

        UUID merchantId = idGenerator.generate();

        Merchant merchant = Merchant.register(
                merchantId,
                command.email(),
                command.legalName(),
                command.businessType(),
                command.country(),
                timeProvider.now()
        );

        merchantRepository.add(merchant);

        return new RegisterMerchantResult(merchantId);
    }
}