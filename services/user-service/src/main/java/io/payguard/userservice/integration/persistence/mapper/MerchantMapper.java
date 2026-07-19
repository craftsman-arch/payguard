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
                .status(merchant.getStatus())
                .createdAt(merchant.getCreatedAt())
                .updatedAt(merchant.getUpdatedAt())
                .build();
    }

    public Merchant toDomain(MerchantEntity entity) {

        return Merchant.restore(
                entity.getId(),
                entity.getEmail(),
                entity.getLegalName(),
                entity.getBusinessType(),
                entity.getCountry(),
                entity.getIdentityUserId(),
                entity.getPaymentAccountId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

}