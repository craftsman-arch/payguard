package io.payguard.userservice.domain.merchant;

import io.payguard.userservice.domain.merchant.value.Email;

import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository {

    void add(Merchant merchant);

    void update(Merchant merchant);

    Optional<Merchant> findById(UUID merchantId);

    Optional<Merchant> findByEmail(Email email);

    boolean existsByEmail(Email email);

    Optional<Merchant> findByIdentityUserId(String identityUserId);

}