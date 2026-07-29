package io.payguard.userservice.integration.web.merchant;

import io.payguard.userservice.application.merchant.query.CurrentMerchantResult;
import io.payguard.userservice.integration.web.merchant.response.CurrentMerchantResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MerchantWebMapper {

    CurrentMerchantResponse toResponse(CurrentMerchantResult result);

}
