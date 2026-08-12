package io.payguard.userservice.domain.user;

import io.payguard.userservice.domain.common.value.Email;
import io.payguard.userservice.domain.user.value.UserId;

import java.util.Optional;

public interface UserRepository {

    void add(User user);

    void update(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(Email email);

    Optional<User> findByIdentityUserId(String identityUserId);
}
