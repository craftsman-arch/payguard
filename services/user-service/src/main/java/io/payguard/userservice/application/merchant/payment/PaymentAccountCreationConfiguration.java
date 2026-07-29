package io.payguard.userservice.application.merchant.payment;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PaymentAccountCreationProperties.class)
public class PaymentAccountCreationConfiguration {
}
