package io.payguard.userservice.application.event.observability;

public interface OutboxOperationalSnapshotRepository {

    OutboxOperationalSnapshot load();
}
