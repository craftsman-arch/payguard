package io.payguard.userservice.application.event;

public enum OutboxEventRecoveryResult {

    RECOVERED,
    EVENT_NOT_FOUND,
    EVENT_NOT_EXHAUSTED,
    EVENT_ALREADY_PUBLISHED
}
