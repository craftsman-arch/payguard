package io.payguard.userservice.application.time;

import java.time.Instant;

public interface TimeProvider {

    Instant now();

}