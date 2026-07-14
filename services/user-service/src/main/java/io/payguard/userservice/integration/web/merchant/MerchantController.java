package io.payguard.userservice.integration.web.merchant;

import io.payguard.userservice.application.merchant.register.RegisterMerchantService;
import io.payguard.userservice.integration.web.merchant.request.RegisterMerchantRequest;
import io.payguard.userservice.integration.web.merchant.response.RegisterMerchantResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final RegisterMerchantService registerMerchantService;
    private final MerchantWebMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterMerchantResponse register(
            @Valid @RequestBody RegisterMerchantRequest request
    ) {

        return mapper.toResponse(
                registerMerchantService.execute(
                        mapper.toCommand(request)
                )
        );
    }

}