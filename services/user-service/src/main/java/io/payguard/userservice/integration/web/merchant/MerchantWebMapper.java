package io.payguard.userservice.integration.web.merchant;

import io.payguard.userservice.application.merchant.query.GetCurrentMerchantResult;
import io.payguard.userservice.application.merchant.register.RegisterMerchantCommand;
import io.payguard.userservice.application.merchant.register.RegisterMerchantResult;
import io.payguard.userservice.domain.merchant.value.Country;
import io.payguard.userservice.domain.merchant.value.Email;
import io.payguard.userservice.integration.web.merchant.request.RegisterMerchantRequest;
import io.payguard.userservice.integration.web.merchant.response.GetCurrentMerchantResponse;
import io.payguard.userservice.integration.web.merchant.response.RegisterMerchantResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MerchantWebMapper {

    default RegisterMerchantCommand toCommand(RegisterMerchantRequest request) {

        Email email = Email.of(request.email());
        Country country = Country.of(request.country());

        return new RegisterMerchantCommand(
                email,
                request.legalName(),
                request.businessType(),
                country
        );
    }

    RegisterMerchantResponse toResponse(RegisterMerchantResult result);

    GetCurrentMerchantResponse toResponse(GetCurrentMerchantResult result);

}