package io.payguard.userservice.application.identity;

import io.payguard.userservice.domain.merchant.value.Email;

public interface IdentityProvider {

    String createUser(Email email, String temporaryPassword);

    void assignRealmRole(String identityUserId, IdentityRole role);

    void deleteUser(String identityUserId);

}