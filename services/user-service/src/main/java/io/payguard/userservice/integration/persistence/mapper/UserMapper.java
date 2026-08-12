package io.payguard.userservice.integration.persistence.mapper;

import io.payguard.userservice.domain.user.User;
import io.payguard.userservice.domain.user.value.UserId;
import io.payguard.userservice.integration.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.getId().value())
                .identityUserId(user.getIdentityUserId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .marketCountry(user.getMarketCountry())
                .status(user.getStatus())
                .revision(user.getRevision())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public User toDomain(UserEntity entity) {
        return User.restore(
                new UserId(entity.getId()),
                entity.getEmail(),
                entity.getDisplayName(),
                entity.getMarketCountry(),
                entity.getIdentityUserId(),
                entity.getStatus(),
                entity.getRevision(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
