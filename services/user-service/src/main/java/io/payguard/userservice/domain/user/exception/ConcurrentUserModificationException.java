package io.payguard.userservice.domain.user.exception;

import io.payguard.userservice.domain.user.value.UserId;

public class ConcurrentUserModificationException extends UserException {

    public ConcurrentUserModificationException(UserId userId) {
        super(
                "User [%s] was concurrently modified."
                        .formatted(userId.value())
        );
    }
}
