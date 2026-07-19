package io.payguard.userservice.application.payment.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum StripeEventType {

    ACCOUNT_UPDATED("account.updated"),

    UNKNOWN("unknown");

    private final String value;

    StripeEventType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static StripeEventType from(
            String value
    ) {

        return Arrays.stream(values())
                .filter(type -> type.value.equals(value))
                .findFirst()
                .orElse(UNKNOWN);
    }

}