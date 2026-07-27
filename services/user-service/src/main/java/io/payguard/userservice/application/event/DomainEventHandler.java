package io.payguard.userservice.application.event;

import io.payguard.userservice.domain.event.DomainEvent;

public interface DomainEventHandler<T extends DomainEvent> {

    Class<T> eventType();

    void handle(T event);
}
