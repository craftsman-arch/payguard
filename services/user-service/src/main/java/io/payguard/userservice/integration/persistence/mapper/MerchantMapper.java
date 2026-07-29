package io.payguard.userservice.integration.persistence.mapper;

import io.payguard.userservice.domain.merchant.Merchant;
import io.payguard.userservice.integration.persistence.entity.MerchantEntity;
import org.springframework.stereotype.Component;

@Component
public class MerchantMapper {

    public MerchantEntity toEntity(Merchant merchant) {

        return MerchantEntity.builder()
                .id(merchant.getId())
                .email(merchant.getEmail())
                .legalName(merchant.getLegalName())
                .businessType(merchant.getBusinessType())
                .country(merchant.getCountry())
                .identityUserId(merchant.getIdentityUserId())
                .paymentAccountId(merchant.getPaymentAccountId())
                .paymentAccountCreationStartedAt(
                        merchant.getPaymentAccountCreationStartedAt()
                )
                .paymentAccountStatus(merchant.getPaymentAccountStatus())
                .paymentAccountStatusReason(merchant.getPaymentAccountStatusReason())
                .paymentAccountRequiredAction(
                        merchant.getPaymentAccountRequiredAction()
                )
                .lastPaymentAccountEventAt(merchant.getLastPaymentAccountEventAt())
                .cardPaymentsCapabilityActive(merchant.isCardPaymentsCapabilityActive())
                .transfersCapabilityActive(merchant.isTransfersCapabilityActive())
                .status(merchant.getStatus())
                .createdAt(merchant.getCreatedAt())
                .updatedAt(merchant.getUpdatedAt())
                .revision(merchant.getRevision())
                .build();
    }

    public Merchant toDomain(MerchantEntity entity) {

        return Merchant.restore(
                entity.getId(),
                entity.getEmail(),
                entity.getLegalName(),
                entity.getBusinessType(),
                entity.getCountry(),
                entity.getRevision(),
                entity.getIdentityUserId(),
                entity.getPaymentAccountId(),
                entity.getPaymentAccountCreationStartedAt(),
                entity.getPaymentAccountStatus(),
                entity.getPaymentAccountStatusReason(),
                entity.getPaymentAccountRequiredAction(),
                entity.getLastPaymentAccountEventAt(),
                entity.isCardPaymentsCapabilityActive(),
                entity.isTransfersCapabilityActive(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

}
