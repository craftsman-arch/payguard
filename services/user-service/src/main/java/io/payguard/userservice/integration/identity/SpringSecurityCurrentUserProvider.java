package io.payguard.userservice.integration.identity;

import io.payguard.userservice.application.identity.CurrentUserProvider;
import io.payguard.userservice.application.identity.AuthenticatedUser;
import io.payguard.userservice.domain.common.value.Email;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public AuthenticatedUser currentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new IllegalStateException("No authenticated JWT principal found.");
        }

        String email = jwtAuthentication.getToken()
                .getClaimAsString("email");

        if (email == null || email.isBlank()) {
            throw new IllegalStateException(
                    "Authenticated JWT does not contain an email claim."
            );
        }

        return new AuthenticatedUser(
                jwtAuthentication.getToken().getSubject(),
                Email.of(email),
                Boolean.TRUE.equals(
                        jwtAuthentication.getToken()
                                .getClaimAsBoolean("email_verified")
                )
        );
    }

}
