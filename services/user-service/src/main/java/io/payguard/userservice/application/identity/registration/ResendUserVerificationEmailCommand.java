package io.payguard.userservice.application.identity.registration;

import io.payguard.userservice.domain.common.value.Email;

public record ResendUserVerificationEmailCommand(

        Email email

) {
}
