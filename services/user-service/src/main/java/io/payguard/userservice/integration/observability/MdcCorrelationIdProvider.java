package io.payguard.userservice.integration.observability;

import io.payguard.userservice.application.observability.CorrelationIdProvider;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class MdcCorrelationIdProvider implements CorrelationIdProvider {

    private static final String TRACE_ID = "traceId";

    @Override
    public String currentCorrelationId() {
        return MDC.get(TRACE_ID);
    }
}
