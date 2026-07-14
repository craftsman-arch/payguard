package io.payguard.userservice.application.merchant.activate;

import java.util.UUID;

public record ActivateMerchantCommand(

        UUID merchantId

) {
}