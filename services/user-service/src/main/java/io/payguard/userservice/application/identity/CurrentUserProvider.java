package io.payguard.userservice.application.identity;

public interface CurrentUserProvider {

    AuthenticatedUser currentUser();

    default String currentUserId() {
        return currentUser().id();
    }

}
