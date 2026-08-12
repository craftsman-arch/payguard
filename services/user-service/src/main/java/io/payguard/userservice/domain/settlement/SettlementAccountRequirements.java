package io.payguard.userservice.domain.settlement;

public record SettlementAccountRequirements(
        boolean userActionRequired,
        boolean verificationPending,
        boolean futureRequirementsPresent
) {

    public static SettlementAccountRequirements empty() {
        return new SettlementAccountRequirements(false, false, false);
    }

    public boolean requiresUserAction() {
        return userActionRequired;
    }

    public boolean isPendingVerification() {
        return verificationPending;
    }

    public boolean hasEventuallyDueRequirements() {
        return futureRequirementsPresent;
    }
}
