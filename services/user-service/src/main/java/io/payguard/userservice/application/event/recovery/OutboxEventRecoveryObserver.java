package io.payguard.userservice.application.event.recovery;

public interface OutboxEventRecoveryObserver {

    void recovered(RecoverOutboxEventResult result);
}
