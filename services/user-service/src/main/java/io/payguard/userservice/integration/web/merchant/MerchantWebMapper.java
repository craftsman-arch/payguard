package io.payguard.userservice.integration.web.merchant;

import io.payguard.userservice.application.merchant.register.RegisterMerchantCommand;
import io.payguard.userservice.application.merchant.register.RegisterMerchantResult;
import io.payguard.userservice.domain.merchant.value.Country;
import io.payguard.userservice.domain.merchant.value.Email;
import io.payguard.userservice.integration.web.merchant.request.RegisterMerchantRequest;
import io.payguard.userservice.integration.web.merchant.response.RegisterMerchantResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MerchantWebMapper {

    default RegisterMerchantCommand toCommand(RegisterMerchantRequest request) {
        return new RegisterMerchantCommand(
                Email.of(request.email()),
                request.legalName(),
                request.businessType(),
                Country.of(request.country())
        );
    }

    @Mapping(target = "id", source = "merchantId")
    RegisterMerchantResponse toResponse(RegisterMerchantResult result);

}