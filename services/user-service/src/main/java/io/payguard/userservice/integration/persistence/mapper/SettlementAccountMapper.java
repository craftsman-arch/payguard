package io.payguard.userservice.integration.persistence.mapper;

import io.payguard.userservice.domain.settlement.SettlementAccount;
import io.payguard.userservice.domain.settlement.value.SettlementAccountId;
import io.payguard.userservice.domain.user.value.UserId;
import io.payguard.userservice.integration.persistence.entity.SettlementAccountEntity;
import org.springframework.stereotype.Component;

@Component
public class SettlementAccountMapper {

    public SettlementAccountEntity toEntity(SettlementAccount account) {

        return SettlementAccountEntity.builder()
                .id(account.getId().value())
                .userId(account.getUserId().value())
                .provider(account.getProvider())
                .providerAccountId(account.getProviderAccountId())
                .accountHolderName(account.getAccountHolderName())
                .accountHolderType(account.getAccountHolderType())
                .country(account.getCountry())
                .creationStartedAt(account.getCreationStartedAt())
                .status(account.getStatus())
                .statusReason(account.getStatusReason())
                .requiredAction(account.getRequiredAction())
                .lastProviderEventAt(account.getLastProviderEventAt())
                .transfersCapabilityActive(account.isTransfersCapabilityActive())
                .revision(account.getRevision())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    public SettlementAccount toDomain(SettlementAccountEntity entity) {

        return SettlementAccount.restore(
                new SettlementAccountId(entity.getId()),
                new UserId(entity.getUserId()),
                entity.getProvider(),
                entity.getAccountHolderName(),
                entity.getAccountHolderType(),
                entity.getCountry(),
                entity.getProviderAccountId(),
                entity.getCreationStartedAt(),
                entity.getStatus(),
                entity.getStatusReason(),
                entity.getRequiredAction(),
                entity.getLastProviderEventAt(),
                entity.isTransfersCapabilityActive(),
                entity.getRevision(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
