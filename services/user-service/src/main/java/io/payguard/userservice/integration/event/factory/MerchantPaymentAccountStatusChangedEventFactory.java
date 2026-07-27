package io.payguard.userservice.integration.event.factory;

import io.payguard.userservice.application.id.IdGenerator;
import io.payguard.userservice.domain.merchant.event.MerchantPaymentStateChanged;
import io.payguard.userservice.integration.event.contracts.EventEnvelope;
import io.payguard.userservice.integration.event.contracts.merchant.v1.MerchantPaymentAccountStatusChanged;
import io.payguard.userservice.integration.event.mapper.MerchantPaymentAccountStatusChangedMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantPaymentAccountStatusChangedEventFactory {

    private static final String SOURCE = "user-service";
    private static final String SUBJECT_PREFIX = "merchant/";

    private final IdGenerator idGenerator;
    private final MerchantPaymentAccountStatusChangedMapper mapper;

    public EventEnvelope<MerchantPaymentAccountStatusChanged> create(
            MerchantPaymentStateChanged domainEvent,
            String correlationId
    ) {

        MerchantPaymentAccountStatusChanged event =
                new MerchantPaymentAccountStatusChanged(
                        domainEvent.merchantId(),
                        mapper.toContract(
                                domainEvent.previousState()
                        ),
                        mapper.toContract(
                                domainEvent.currentState()
                        )
                );

        return new EventEnvelope<>(
                idGenerator.generate(),
                MerchantPaymentAccountStatusChanged.TYPE,
                SOURCE,
                domainEvent.occurredAt(),
                SUBJECT_PREFIX + domainEvent.merchantId(),
                normalizeCorrelationId(correlationId),
                event
        );
    }

    private String normalizeCorrelationId(String correlationId) {

        return correlationId == null || correlationId.isBlank()
                ? null
                : correlationId;
    }
}
