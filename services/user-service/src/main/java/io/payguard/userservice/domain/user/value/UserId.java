package io.payguard.userservice.domain.user.value;

import lombok.NonNull;

import java.util.UUID;

public record UserId(@NonNull UUID value) {

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
