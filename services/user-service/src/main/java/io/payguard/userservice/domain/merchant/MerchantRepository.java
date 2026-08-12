package io.payguard.userservice.domain.merchant;

import io.payguard.userservice.domain.common.value.Email;

import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository {

    void add(Merchant merchant);

    void update(Merchant merchant);

    Optional<Merchant> findById(UUID id);

    Optional<Merchant> findByEmail(Email email);

    Optional<Merchant> findByIdentityUserId(String identityUserId);

    Optional<Merchant> findByPaymentAccountId(String paymentAccountId);

}