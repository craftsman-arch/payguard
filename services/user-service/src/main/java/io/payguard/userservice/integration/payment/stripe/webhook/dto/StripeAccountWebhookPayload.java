package io.payguard.userservice.integration.payment.stripe.webhook.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Locale;

import static java.util.Optional.ofNullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StripeAccountWebhookPayload(

        String id,

        @JsonProperty("payouts_enabled")
        boolean payoutsEnabled,

        Capabilities capabilities,

        Requirements requirements
) {

    public StripeAccountWebhookPayload {

        capabilities = ofNullable(capabilities)
                .orElseGet(Capabilities::unknown);

        requirements = ofNullable(requirements)
                .orElseGet(Requirements::empty);

    }

    public String disabledReason() {
        return requirements.disabledReason();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Capabilities(
            @JsonProperty("card_payments")
            CapabilityStatus cardPayments,
            CapabilityStatus transfers
    ) {

        public Capabilities {
            cardPayments = ofNullable(cardPayments)
                    .orElse(CapabilityStatus.UNKNOWN);

            transfers = ofNullable(transfers)
                    .orElse(CapabilityStatus.UNKNOWN);
        }

        public static Capabilities unknown() {

            return new Capabilities(
                    CapabilityStatus.UNKNOWN,
                    CapabilityStatus.UNKNOWN
            );
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Requirements(

            @JsonProperty("disabled_reason")
            String disabledReason,

            @JsonProperty("currently_due")
            List<String> currentlyDue,

            @JsonProperty("past_due")
            List<String> pastDue,

            @JsonProperty("pending_verification")
            List<String> pendingVerification,

            @JsonProperty("eventually_due")
            List<String> eventuallyDue
    ) {

        public Requirements {

            currentlyDue = immutableCopy(currentlyDue);
            pastDue = immutableCopy(pastDue);
            pendingVerification = immutableCopy(pendingVerification);
            eventuallyDue = immutableCopy(eventuallyDue);
        }

        public static Requirements empty() {

            return new Requirements(
                    null,
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of()
            );
        }

        private static List<String> immutableCopy(List<String> values) {

            return List.copyOf(
                    ofNullable(values)
                            .orElseGet(List::of)
            );
        }
    }

    public enum CapabilityStatus {

        ACTIVE,
        PENDING,
        INACTIVE,
        UNKNOWN;

        @JsonCreator
        public static CapabilityStatus fromValue(String value) {

            if (value == null || value.isBlank()) {
                return UNKNOWN;
            }

            try {
                return valueOf(value.toUpperCase(Locale.ROOT));

            } catch (IllegalArgumentException exception) {
                return UNKNOWN;
            }
        }

        public boolean isActive() {
            return this == ACTIVE;
        }
    }
}
