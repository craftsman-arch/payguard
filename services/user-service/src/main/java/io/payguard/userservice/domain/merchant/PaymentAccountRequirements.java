package io.payguard.userservice.domain.merchant;

import java.util.List;

import static java.util.Optional.ofNullable;

public record PaymentAccountRequirements(

        List<String> currentlyDue,
        List<String> pastDue,
        List<String> pendingVerification,
        List<String> eventuallyDue
) {

    public PaymentAccountRequirements {

        currentlyDue = immutableCopy(currentlyDue);
        pastDue = immutableCopy(pastDue);
        pendingVerification = immutableCopy(pendingVerification);
        eventuallyDue = immutableCopy(eventuallyDue);
    }

    public static PaymentAccountRequirements empty() {

        return new PaymentAccountRequirements(
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    public boolean requiresMerchantAction() {

        return !currentlyDue.isEmpty()
                || !pastDue.isEmpty();
    }

    public boolean isPendingVerification() {

        return !pendingVerification.isEmpty();
    }

    public boolean hasEventuallyDueRequirements() {

        return !eventuallyDue.isEmpty();
    }

    private static List<String> immutableCopy(List<String> values) {

        return List.copyOf(
                ofNullable(values)
                        .orElseGet(List::of)
        );
    }
}