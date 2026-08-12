package io.payguard.userservice.integration.persistence.repository;

import io.payguard.userservice.domain.common.value.Email;
import io.payguard.userservice.domain.user.UserStatus;
import io.payguard.userservice.integration.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(Email email);

    Optional<UserEntity> findByIdentityUserId(String identityUserId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserEntity user
               SET user.status = :status,
                   user.updatedAt = :updatedAt,
                   user.revision = user.revision + 1
             WHERE user.id = :id
               AND user.revision = :expectedRevision
            """)
    int updateIfRevisionMatches(
            @Param("id") UUID id,
            @Param("status") UserStatus status,
            @Param("updatedAt") Instant updatedAt,
            @Param("expectedRevision") long expectedRevision
    );
}
