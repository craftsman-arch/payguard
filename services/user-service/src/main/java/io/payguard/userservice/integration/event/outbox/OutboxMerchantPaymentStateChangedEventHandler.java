package io.payguard.userservice.integration.event.outbox;

import io.payguard.userservice.application.event.DomainEventHandler;
import io.payguard.userservice.application.event.OutboxEvent;
import io.payguard.userservice.application.event.OutboxEventRepository;
import io.payguard.userservice.application.observability.CorrelationIdProvider;
import io.payguard.userservice.domain.merchant.event.MerchantPaymentStateChanged;
import io.payguard.userservice.integration.event.contracts.EventEnvelope;
import io.payguard.userservice.integration.event.contracts.merchant.v1.MerchantPaymentAccountStatusChanged;
import io.payguard.userservice.integration.event.factory.MerchantPaymentAccountStatusChangedEventFactory;
import io.payguard.userservice.integration.event.serialization.EventSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxMerchantPaymentStateChangedEventHandler
        implements DomainEventHandler<MerchantPaymentStateChanged> {

    private static final String AGGREGATE_TYPE = "MERCHANT";

    private final MerchantPaymentAccountStatusChangedEventFactory eventFactory;
    private final EventSerializer eventSerializer;
    private final OutboxEventRepository outboxEventRepository;
    private final CorrelationIdProvider correlationIdProvider;

    @Override
    public Class<MerchantPaymentStateChanged> eventType() {
        return MerchantPaymentStateChanged.class;
    }

    @Override
    public void handle(MerchantPaymentStateChanged domainEvent) {

        EventEnvelope<MerchantPaymentAccountStatusChanged> envelope =
                eventFactory.create(
                        domainEvent,
                        correlationIdProvider.currentCorrelationId()
                );

        String eventBody = eventSerializer.serialize(envelope);

        OutboxEvent outboxEvent =
                new OutboxEvent(
                        envelope.id(),
                        AGGREGATE_TYPE,
                        domainEvent.merchantId().toString(),
                        envelope.type(),
                        eventBody,
                        envelope.time(),
                        envelope.correlationId()
                );

        outboxEventRepository.add(outboxEvent);

        log.info(
                "Recorded domain event [{}] for merchant [{}] in the transactional outbox.",
                envelope.type(),
                domainEvent.merchantId()
        );
    }
}
