package io.payguard.userservice.application.payment.events;

import com.fasterxml.jackson.databind.JsonNode;
import io.payguard.userservice.integration.payment.events.StripeEventReader;
import io.payguard.userservice.integration.payment.events.dto.StripeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripeEventDispatcher {

    private final List<StripeEventHandler<?>> handlers;
    private final StripeEventReader eventReader;

    public void dispatch(StripeEvent event) {

        handlers.stream()
                .filter(handler ->
                        handler.supports() == event.type()
                )
                .findFirst()
                .ifPresentOrElse(

                        handler -> dispatch(
                                handler,
                                event.data().object()
                        ),

                        () -> log.info(
                                "Ignoring unsupported Stripe event [{}].",
                                event.type()
                        )
                );
    }

    private <T> void dispatch(StripeEventHandler<T> handler, JsonNode payload) {

        T dto = eventReader.read(payload, handler.eventClass());
        handler.handle(dto);
    }

}