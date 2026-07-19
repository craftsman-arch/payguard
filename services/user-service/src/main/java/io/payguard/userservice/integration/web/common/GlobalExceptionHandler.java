package io.payguard.userservice.integration.web.common;

import io.payguard.userservice.application.time.TimeProvider;
import io.payguard.userservice.domain.merchant.exception.MerchantAlreadyExistsException;
import io.payguard.userservice.domain.merchant.exception.MerchantException;
import io.payguard.userservice.domain.merchant.exception.MerchantNotFoundException;
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

    private final TimeProvider timeProvider;

    @ExceptionHandler(MerchantAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            MerchantAlreadyExistsException exception,
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

    @ExceptionHandler(MerchantException.class)
    public ResponseEntity<ErrorResponse> handleMerchantException(
            MerchantException exception,
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