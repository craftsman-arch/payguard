package io.payguard.userservice.integration.persistence.repository.impl;

import io.payguard.userservice.application.payment.webhook.PaymentProviderEventType;
import io.payguard.userservice.application.payment.webhook.ProcessedPaymentProviderEventRepository;
import io.payguard.userservice.integration.persistence.repository.ProcessedStripeEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class ProcessedPaymentProviderEventRepositoryImpl
        implements ProcessedPaymentProviderEventRepository {

    private final ProcessedStripeEventJpaRepository repository;

    @Override
    public boolean tryClaim(String eventId, PaymentProviderEventType eventType, Instant processedAt) {

        return repository.tryInsert(eventId, eventType.name(), processedAt) == 1;
    }
}