package io.payguard.userservice.integration.web.user;

import io.payguard.userservice.application.user.query.CurrentUserResult;
import io.payguard.userservice.integration.web.user.response.CurrentUserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserWebMapper {

    CurrentUserResponse toResponse(CurrentUserResult result);
}
