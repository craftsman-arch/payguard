package io.payguard.userservice.integration.persistence.repository.impl;

import io.payguard.userservice.domain.merchant.Merchant;
import io.payguard.userservice.domain.merchant.MerchantRepository;
import io.payguard.userservice.domain.merchant.exception.ConcurrentMerchantModificationException;
import io.payguard.userservice.domain.merchant.exception.MerchantAlreadyExistsException;
import io.payguard.userservice.domain.merchant.value.Email;
import io.payguard.userservice.integration.persistence.mapper.MerchantMapper;
import io.payguard.userservice.integration.persistence.repository.MerchantJpaRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MerchantRepositoryImpl implements MerchantRepository {

    private static final String EMAIL_UNIQUE_CONSTRAINT =
            "uk_merchants_email";

    private static final String IDENTITY_UNIQUE_CONSTRAINT =
            "uk_merchants_identity_user_id";

    private final MerchantJpaRepository repository;
    private final MerchantMapper mapper;

    @Override
    public void add(Merchant merchant) {

        try {

            repository.saveAndFlush(
                    mapper.toEntity(merchant)
            );

        } catch (DataIntegrityViolationException exception) {

            if (isMerchantIdentityConflict(exception)) {
                throw new MerchantAlreadyExistsException(
                        merchant.getEmail(),
                        exception
                );
            }

            throw exception;
        }
    }

    private boolean isMerchantIdentityConflict(Throwable exception) {

        Throwable cause = exception;

        while (cause != null) {

            if (cause instanceof ConstraintViolationException violation) {

                String constraintName =
                        violation.getConstraintName();

                return EMAIL_UNIQUE_CONSTRAINT.equalsIgnoreCase(
                        constraintName
                ) || IDENTITY_UNIQUE_CONSTRAINT.equalsIgnoreCase(
                        constraintName
                );
            }

            cause = cause.getCause();
        }

        return false;
    }

    @Override
    public void update(Merchant merchant) {

        try {
            repository.saveAndFlush(mapper.toEntity(merchant));

        } catch (ObjectOptimisticLockingFailureException exception) {

            throw new ConcurrentMerchantModificationException(
                    merchant.getId(),
                    exception
            );
        }
    }

    @Override
    public Optional<Merchant> findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Merchant> findByEmail(Email email) {
        return repository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Merchant> findByIdentityUserId(String identityUserId) {

        return repository
                .findByIdentityUserId(identityUserId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Merchant> findByPaymentAccountId(
            String paymentAccountId
    ) {

        return repository.findByPaymentAccountId(
                        paymentAccountId
                )
                .map(mapper::toDomain);
    }
}
