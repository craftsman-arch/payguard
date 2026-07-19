package io.payguard.userservice.integration.persistence.repository.impl;

import io.payguard.userservice.domain.merchant.Merchant;
import io.payguard.userservice.domain.merchant.MerchantRepository;
import io.payguard.userservice.domain.merchant.value.Email;
import io.payguard.userservice.integration.persistence.mapper.MerchantMapper;
import io.payguard.userservice.integration.persistence.repository.MerchantJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MerchantRepositoryImpl implements MerchantRepository {

    private final MerchantJpaRepository repository;
    private final MerchantMapper mapper;

    @Override
    public void add(Merchant merchant) {
        repository.save(mapper.toEntity(merchant));
    }

    @Override
    public void update(Merchant merchant) {
        repository.save(mapper.toEntity(merchant));
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