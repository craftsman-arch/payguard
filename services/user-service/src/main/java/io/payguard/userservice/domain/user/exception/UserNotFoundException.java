package io.payguard.userservice.domain.user.exception;

public class UserNotFoundException extends UserException {

    public UserNotFoundException(String identityUserId) {
        super(
                "User with identity user id [%s] was not found."
                        .formatted(identityUserId)
        );
    }
}
