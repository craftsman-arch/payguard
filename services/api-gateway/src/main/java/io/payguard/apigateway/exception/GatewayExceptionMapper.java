package io.payguard.apigateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class GatewayExceptionMapper {

    public HttpStatus map(Throwable exception) {

        if (exception instanceof ResponseStatusException ex) {
            return HttpStatus.valueOf(ex.getStatusCode().value());
        }

        if (exception instanceof AuthenticationException) {
            return HttpStatus.UNAUTHORIZED;
        }

        if (exception instanceof AccessDeniedException) {
            return HttpStatus.FORBIDDEN;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}