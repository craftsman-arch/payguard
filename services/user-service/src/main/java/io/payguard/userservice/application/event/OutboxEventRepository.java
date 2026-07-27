package io.payguard.userservice.application.event;

public interface OutboxEventRepository {

    void add(OutboxEvent event);
}
