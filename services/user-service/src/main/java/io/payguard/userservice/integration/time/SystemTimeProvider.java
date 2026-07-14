package io.payguard.userservice.integration.time;

import io.payguard.userservice.application.time.TimeProvider;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SystemTimeProvider implements TimeProvider {

    @Override
    public Instant now() {
        return Instant.now();
    }

}