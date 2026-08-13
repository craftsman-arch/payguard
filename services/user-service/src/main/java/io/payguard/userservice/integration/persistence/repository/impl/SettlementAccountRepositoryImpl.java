package io.payguard.userservice.integration.persistence.repository.impl;

import io.payguard.userservice.domain.settlement.SettlementAccount;
import io.payguard.userservice.domain.settlement.SettlementAccountRepository;
import io.payguard.userservice.domain.settlement.SettlementProvider;
import io.payguard.userservice.domain.settlement.exception.ConcurrentSettlementAccountModificationException;
import io.payguard.userservice.domain.settlement.exception.SettlementAccountAlreadyExistsException;
import io.payguard.userservice.domain.settlement.value.SettlementAccountId;
import io.payguard.userservice.domain.user.value.UserId;
import io.payguard.userservice.integration.persistence.mapper.SettlementAccountMapper;
import io.payguard.userservice.integration.persistence.repository.SettlementAccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SettlementAccountRepositoryImpl
        implements SettlementAccountRepository {

    private static final String USER_UNIQUE_CONSTRAINT =
            "uk_settlement_accounts_user_id";

    private static final String PROVIDER_ACCOUNT_UNIQUE_CONSTRAINT =
            "uk_settlement_accounts_provider_account";

    private final SettlementAccountJpaRepository repository;
    private final SettlementAccountMapper mapper;

    @Override
    public void add(SettlementAccount account) {
        try {
            repository.saveAndFlush(mapper.toEntity(account));

        } catch (DataIntegrityViolationException exception) {
            if (isSettlementAccountConflict(exception)) {
                throw new SettlementAccountAlreadyExistsException(
                        account.getUserId(),
                        exception
                );
            }

            throw exception;
        }
    }

    @Override
    public void update(SettlementAccount account) {
        int updatedAccounts = repository.updateIfRevisionMatches(
                account.getId().value(),
                account.getProviderAccountId(),
                account.getCreationStartedAt(),
                account.getStatus(),
                account.getStatusReason(),
                account.getRequiredAction(),
                account.getLastProviderEventAt(),
                account.isTransfersCapabilityActive(),
                account.getUpdatedAt(),
                account.getRevision()
        );

        if (updatedAccounts != 1) {
            throw new ConcurrentSettlementAccountModificationException(
                    account.getId()
            );
        }
    }

    @Override
    public Optional<SettlementAccount> findById(SettlementAccountId id) {
        return repository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<SettlementAccount> findByUserId(UserId userId) {
        return repository.findByUserId(userId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<SettlementAccount> findByProviderAccountId(
            SettlementProvider provider,
            String providerAccountId
    ) {
        return repository.findByProviderAndProviderAccountId(
                        provider,
                        providerAccountId
                )
                .map(mapper::toDomain);
    }

    private boolean isSettlementAccountConflict(Throwable exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                String constraintName = violation.getConstraintName();

                return USER_UNIQUE_CONSTRAINT.equalsIgnoreCase(
                        constraintName
                ) || PROVIDER_ACCOUNT_UNIQUE_CONSTRAINT.equalsIgnoreCase(
                        constraintName
                );
            }

            cause = cause.getCause();
        }

        return false;
    }
}
