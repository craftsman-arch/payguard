package io.payguard.userservice.integration.id;

import io.payguard.userservice.application.id.IdGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidGenerator implements IdGenerator {

    @Override
    public UUID generate() {
        return UUID.randomUUID();
    }

}