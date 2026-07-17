package io.payguard.userservice.application.merchant.query;

import io.payguard.userservice.application.identity.CurrentUserProvider;
import io.payguard.userservice.domain.merchant.Merchant;
import io.payguard.userservice.domain.merchant.MerchantRepository;
import io.payguard.userservice.domain.merchant.exception.MerchantNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCurrentMerchantService {

    private final MerchantRepository merchantRepository;
    private final CurrentUserProvider currentUserProvider;

    public GetCurrentMerchantResult execute(
            GetCurrentMerchantQuery query
    ) {

        String identityUserId = currentUserProvider.currentUserId();

        Merchant merchant = merchantRepository
                .findByIdentityUserId(identityUserId)
                .orElseThrow(() -> new MerchantNotFoundException(
                        "Merchant with identity user id '%s' was not found."
                                .formatted(identityUserId)
                ));

        return new GetCurrentMerchantResult(
                merchant.getId(),
                merchant.getEmail().getValue(),
                merchant.getLegalName(),
                merchant.getBusinessType(),
                merchant.getCountry().getValue(),
                merchant.getStatus(),
                merchant.getCreatedAt(),
                merchant.getUpdatedAt()
        );
    }

}