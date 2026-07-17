package io.payguard.userservice.integration.identity;

import io.payguard.userservice.application.identity.CurrentUserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public String currentUserId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new IllegalStateException("No authenticated JWT principal found.");
        }

        return jwtAuthentication.getToken().getSubject();
    }

}