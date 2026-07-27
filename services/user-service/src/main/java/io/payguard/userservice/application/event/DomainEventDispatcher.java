package io.payguard.userservice.application.event;

import io.payguard.userservice.domain.event.DomainEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DomainEventDispatcher {

    private final Map<Class<? extends DomainEvent>, DomainEventHandler<?>> handlers;

    public DomainEventDispatcher(List<DomainEventHandler<?>> handlers) {

        this.handlers = indexHandlers(handlers);
    }

    public void dispatch(List<DomainEvent> events) {
        events.forEach(this::dispatch);
    }

    private void dispatch(DomainEvent event) {

        DomainEventHandler<?> handler = handlers.get(event.getClass());

        if (handler == null) {
            throw new IllegalStateException(
                    "No domain event handler registered for [%s]."
                            .formatted(event.getClass().getName())
            );
        }

        dispatchToHandler(handler, event);
    }

    private <T extends DomainEvent> void dispatchToHandler(DomainEventHandler<T> handler, DomainEvent event) {

        T typedEvent = handler.eventType().cast(event);
        handler.handle(typedEvent);
    }

    private Map<Class<? extends DomainEvent>, DomainEventHandler<?>> indexHandlers(List<DomainEventHandler<?>> handlers) {

        Map<Class<? extends DomainEvent>, DomainEventHandler<?>> indexedHandlers = new HashMap<>();

        for (DomainEventHandler<?> handler : handlers) {

            DomainEventHandler<?> duplicate = indexedHandlers.putIfAbsent(handler.eventType(), handler);

            if (duplicate != null) {
                throw new IllegalStateException(
                        "Multiple domain event handlers registered for [%s]."
                                .formatted(
                                        handler.eventType().getName()
                                )
                );
            }
        }

        return Map.copyOf(indexedHandlers);
    }
}
