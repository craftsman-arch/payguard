package io.payguard.userservice.integration.web.common;

import io.payguard.userservice.application.event.recovery.OutboxEventAlreadyPublishedException;
import io.payguard.userservice.application.event.recovery.OutboxEventNotExhaustedException;
import io.payguard.userservice.application.event.recovery.OutboxEventNotFoundException;
import io.payguard.userservice.application.time.TimeProvider;
import io.payguard.userservice.domain.common.exception.DomainValueException;
import io.payguard.userservice.domain.merchant.exception.MerchantAlreadyExistsException;
import io.payguard.userservice.domain.settlement.exception.ConcurrentSettlementAccountModificationException;
import io.payguard.userservice.domain.settlement.exception.SettlementAccountAlreadyExistsException;
import io.payguard.userservice.domain.settlement.exception.SettlementAccountException;
import io.payguard.userservice.domain.user.exception.ConcurrentUserModificationException;
import io.payguard.userservice.domain.user.exception.UserAlreadyExistsException;
import io.payguard.userservice.domain.user.exception.UserException;
import io.payguard.userservice.domain.user.exception.UnverifiedUserIdentityException;
import io.payguard.userservice.domain.merchant.exception.MerchantException;
import io.payguard.userservice.domain.merchant.exception.MerchantNotFoundException;
import io.payguard.userservice.domain.merchant.exception.UnverifiedMerchantIdentityException;
import io.payguard.userservice.domain.merchant.exception.PaymentAccountReconciliationRequiredException;
import io.payguard.userservice.integration.identity.exception.KeycloakAuthenticationException;
import io.payguard.userservice.integration.identity.exception.KeycloakException;
import io.payguard.userservice.integration.identity.exception.KeycloakPasswordPolicyException;
import io.payguard.userservice.integration.identity.exception.KeycloakUnavailableException;
import io.payguard.userservice.integration.identity.exception.KeycloakUserConflictException;
import io.payguard.userservice.integration.payment.error.exception.StripeProviderRejectedException;
import io.payguard.userservice.integration.payment.error.exception.StripeProviderResponseException;
import io.payguard.userservice.integration.payment.error.exception.StripeProviderUnavailableException;
import io.payguard.userservice.integration.payment.error.exception.StripeSignatureVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final String INTERNAL_SERVER_ERROR_MESSAGE =
            "An unexpected error occurred.";

    private static final String VALIDATION_FAILED_MESSAGE =
            "Validation failed.";

    private static final String ACCESS_DENIED_MESSAGE =
            "Access denied.";

    private static final String PAYMENT_PROVIDER_UNAVAILABLE_MESSAGE =
            "Payment provider is temporarily unavailable.";

    private static final String PAYMENT_PROVIDER_REJECTED_MESSAGE =
            "Payment provider rejected the request.";

    private static final String PAYMENT_PROVIDER_ERROR_MESSAGE =
            "Payment provider failed to process the request.";

    private static final String INVALID_WEBHOOK_SIGNATURE_MESSAGE =
            "Invalid webhook signature.";

    private static final String IDENTITY_PROVIDER_UNAVAILABLE_MESSAGE =
            "Identity provider is temporarily unavailable.";

    private static final String IDENTITY_PROVIDER_ERROR_MESSAGE =
            "Identity provider failed to process the request.";

    private static final String IDENTITY_ALREADY_EXISTS_MESSAGE =
            "User identity already exists.";

    private static final String PASSWORD_POLICY_REJECTED_MESSAGE =
            "Password does not satisfy the security policy.";

    private static final String PAYMENT_ACCOUNT_RECONCILIATION_MESSAGE =
            "Payment account creation requires manual reconciliation.";

    private final TimeProvider timeProvider;

    @ExceptionHandler(OutboxEventNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOutboxEventNotFound(
            OutboxEventNotFoundException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                exception,
                request
        );
    }

    @ExceptionHandler({
            OutboxEventNotExhaustedException.class,
            OutboxEventAlreadyPublishedException.class
    })
    public ResponseEntity<ErrorResponse> handleOutboxEventRecoveryConflict(
            RuntimeException exception,
            HttpServletRequest request
    ) {

        log.warn(
                "Rejected outbox event recovery request [{} {}]: {}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getMessage()
        );

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                exception,
                request
        );
    }

    @ExceptionHandler(PaymentAccountReconciliationRequiredException.class)
    public ResponseEntity<ErrorResponse> handlePaymentAccountReconciliation(
            PaymentAccountReconciliationRequiredException exception,
            HttpServletRequest request
    ) {

        log.error(
                "Payment-account creation requires reconciliation while processing request [{} {}].",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                PAYMENT_ACCOUNT_RECONCILIATION_MESSAGE,
                request
        );
    }

    @ExceptionHandler({
            UnverifiedMerchantIdentityException.class,
            UnverifiedUserIdentityException.class
    })
    public ResponseEntity<ErrorResponse> handleUnverifiedUserIdentity(
            RuntimeException exception,
            HttpServletRequest request
    ) {

        log.warn(
                "Unverified user identity attempted to create a profile [{} {}].",
                request.getMethod(),
                request.getRequestURI()
        );

        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                exception,
                request
        );
    }

    @ExceptionHandler({
            MerchantAlreadyExistsException.class,
            UserAlreadyExistsException.class,
            ConcurrentUserModificationException.class,
            SettlementAccountAlreadyExistsException.class,
            ConcurrentSettlementAccountModificationException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.CONFLICT, exception, request);
    }

    @ExceptionHandler(MerchantNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            MerchantNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, exception, request);
    }

    @ExceptionHandler({
            MerchantException.class,
            UserException.class,
            SettlementAccountException.class,
            DomainValueException.class
    })
    public ResponseEntity<ErrorResponse> handleDomainException(
            RuntimeException exception,
            HttpServletRequest request
    ) {

        log.warn("Business exception while processing request [{} {}]: {}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getMessage());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception,
                request
        );
    }

    @ExceptionHandler(StripeProviderUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleStripeProviderUnavailable(
            StripeProviderUnavailableException exception,
            HttpServletRequest request
    ) {

        log.error(
                "Stripe is unavailable while processing request [{} {}].",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                PAYMENT_PROVIDER_UNAVAILABLE_MESSAGE,
                request
        );
    }

    @ExceptionHandler(StripeProviderRejectedException.class)
    public ResponseEntity<ErrorResponse> handleStripeProviderRejected(
            StripeProviderRejectedException exception,
            HttpServletRequest request
    ) {

        log.warn(
                "Stripe rejected request [{} {}].",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return buildErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                PAYMENT_PROVIDER_REJECTED_MESSAGE,
                request
        );
    }

    @ExceptionHandler(StripeProviderResponseException.class)
    public ResponseEntity<ErrorResponse> handleStripeProviderResponse(
            StripeProviderResponseException exception,
            HttpServletRequest request
    ) {

        log.error(
                "Unexpected Stripe response while processing request [{} {}].",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return buildErrorResponse(
                HttpStatus.BAD_GATEWAY,
                PAYMENT_PROVIDER_ERROR_MESSAGE,
                request
        );
    }

    @ExceptionHandler(StripeSignatureVerificationException.class)
    public ResponseEntity<ErrorResponse> handleStripeSignatureVerification(
            StripeSignatureVerificationException exception,
            HttpServletRequest request
    ) {

        log.warn(
                "Rejected Stripe webhook with an invalid signature [{} {}]: {}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getMessage()
        );

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                INVALID_WEBHOOK_SIGNATURE_MESSAGE,
                request
        );
    }

    @ExceptionHandler(KeycloakPasswordPolicyException.class)
    public ResponseEntity<ErrorResponse> handleKeycloakPasswordPolicy(
            KeycloakPasswordPolicyException exception,
            HttpServletRequest request
    ) {

        log.warn(
                "Keycloak rejected a password while processing request [{} {}].",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return buildErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                PASSWORD_POLICY_REJECTED_MESSAGE,
                request
        );
    }

    @ExceptionHandler(KeycloakUserConflictException.class)
    public ResponseEntity<ErrorResponse> handleKeycloakUserConflict(
            KeycloakUserConflictException exception,
            HttpServletRequest request
    ) {

        log.warn(
                "Keycloak identity conflict while processing request [{} {}].",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                IDENTITY_ALREADY_EXISTS_MESSAGE,
                request
        );
    }

    @ExceptionHandler({
            KeycloakUnavailableException.class,
            KeycloakAuthenticationException.class
    })
    public ResponseEntity<ErrorResponse> handleKeycloakUnavailable(
            KeycloakException exception,
            HttpServletRequest request
    ) {

        log.error(
                "Keycloak is unavailable while processing request [{} {}].",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                IDENTITY_PROVIDER_UNAVAILABLE_MESSAGE,
                request
        );
    }

    @ExceptionHandler(KeycloakException.class)
    public ResponseEntity<ErrorResponse> handleKeycloakException(
            KeycloakException exception,
            HttpServletRequest request
    ) {

        log.error(
                "Unexpected Keycloak response while processing request [{} {}].",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return buildErrorResponse(
                HttpStatus.BAD_GATEWAY,
                IDENTITY_PROVIDER_ERROR_MESSAGE,
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        List<ValidationError> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationError(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        log.warn("Validation failed while processing request [{} {}]",
                request.getMethod(),
                request.getRequestURI());

        return ResponseEntity.status(status)
                .body(
                        new ValidationErrorResponse(
                                timeProvider.now(),
                                status.value(),
                                status.getReasonPhrase(),
                                VALIDATION_FAILED_MESSAGE,
                                request.getRequestURI(),
                                errors
                        )
                );
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDenied(
            AuthorizationDeniedException exception,
            HttpServletRequest request
    ) {

        log.warn("Access denied while processing request [{} {}]",
                request.getMethod(),
                request.getRequestURI());

        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                ACCESS_DENIED_MESSAGE,
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {

        log.error("Unexpected exception while processing request [{} {}]",
                request.getMethod(),
                request.getRequestURI(),
                exception);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                INTERNAL_SERVER_ERROR_MESSAGE,
                request
        );
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            Exception exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                status,
                exception.getMessage(),
                request
        );
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {

        return ResponseEntity.status(status)
                .body(
                        new ErrorResponse(
                                timeProvider.now(),
                                status.value(),
                                status.getReasonPhrase(),
                                message,
                                request.getRequestURI()
                        )
                );
    }

}
