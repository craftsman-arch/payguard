package io.payguard.userservice.integration.persistence.repository.impl;

import io.payguard.userservice.domain.common.value.Email;
import io.payguard.userservice.domain.user.User;
import io.payguard.userservice.domain.user.UserRepository;
import io.payguard.userservice.domain.user.exception.ConcurrentUserModificationException;
import io.payguard.userservice.domain.user.exception.UserAlreadyExistsException;
import io.payguard.userservice.domain.user.value.UserId;
import io.payguard.userservice.integration.persistence.mapper.UserMapper;
import io.payguard.userservice.integration.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private static final String EMAIL_UNIQUE_CONSTRAINT = "uk_users_email";
    private static final String IDENTITY_UNIQUE_CONSTRAINT = "uk_users_identity_user_id";

    private final UserJpaRepository repository;
    private final UserMapper mapper;

    @Override
    public void add(User user) {
        try {
            repository.saveAndFlush(mapper.toEntity(user));

        } catch (DataIntegrityViolationException exception) {
            if (isUserIdentityConflict(exception)) {
                throw new UserAlreadyExistsException(
                        user.getEmail(),
                        exception
                );
            }

            throw exception;
        }
    }

    @Override
    public void update(User user) {
        int updatedUsers = repository.updateIfRevisionMatches(
                user.getId().value(),
                user.getStatus(),
                user.getUpdatedAt(),
                user.getRevision()
        );

        if (updatedUsers != 1) {
            throw new ConcurrentUserModificationException(
                    user.getId()
            );
        }
    }

    @Override
    public Optional<User> findById(UserId id) {
        return repository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return repository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByIdentityUserId(String identityUserId) {
        return repository.findByIdentityUserId(identityUserId)
                .map(mapper::toDomain);
    }

    private boolean isUserIdentityConflict(Throwable exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                String constraintName = violation.getConstraintName();

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
}
