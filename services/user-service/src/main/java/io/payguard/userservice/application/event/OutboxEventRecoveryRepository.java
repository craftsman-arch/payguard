package io.payguard.userservice.application.event;

import java.util.UUID;

public interface OutboxEventRecoveryRepository {

    OutboxEventRecoveryResult recover(
            UUID recoveryId,
            UUID eventId,
            String recoveredBy,
            String reason
    );
}
