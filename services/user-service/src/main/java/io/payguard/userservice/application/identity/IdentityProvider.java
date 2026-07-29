package io.payguard.userservice.application.identity;

import io.payguard.userservice.domain.merchant.value.Email;

public interface IdentityProvider {

    String createUser(Email email, String password);

    void assignRealmRole(String identityUserId, IdentityRole role);

    void sendVerificationEmail(String identityUserId);

    void resumeMerchantIdentityRegistration(Email email);

}
