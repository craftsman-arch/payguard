package io.payguard.userservice.integration.persistence.repository;

import io.payguard.userservice.domain.merchant.value.Email;
import io.payguard.userservice.integration.persistence.entity.MerchantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MerchantJpaRepository extends JpaRepository<MerchantEntity, UUID> {

    Optional<MerchantEntity> findByEmail(Email email);

    boolean existsByEmail(Email email);

    Optional<MerchantEntity> findByIdentityUserId(String identityUserId);

}