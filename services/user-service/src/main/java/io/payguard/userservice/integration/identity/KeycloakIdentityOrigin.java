package io.payguard.userservice.integration.identity;

public final class KeycloakIdentityOrigin {

    public static final String ATTRIBUTE = "payguard.internal.identity_origin";
    public static final String USER_SELF_REGISTRATION = "USER_SELF_REGISTRATION";
    public static final String LEGACY_MERCHANT_SELF_REGISTRATION = "MERCHANT_SELF_REGISTRATION";

    private KeycloakIdentityOrigin() {}
}
