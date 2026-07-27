package io.payguard.userservice.application.observability;

public interface CorrelationIdProvider {

    String currentCorrelationId();
}
