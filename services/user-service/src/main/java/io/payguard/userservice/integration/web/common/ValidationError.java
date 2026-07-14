package io.payguard.userservice.integration.web.common;

public record ValidationError(

        String field,

        String message

) {
}