package io.payguard.userservice.application.payment.webhook;

import io.payguard.userservice.domain.merchant.exception.ConcurrentMerchantModificationException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.UniformRandomBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Map;

@Configuration
@EnableConfigurationProperties(PaymentProviderWebhookConcurrencyRetryProperties.class)
public class PaymentProviderWebhookConcurrencyRetryConfiguration {

    public static final String RETRY_TEMPLATE =
            "paymentProviderWebhookConcurrencyRetryTemplate";

    @Bean(RETRY_TEMPLATE)
    public RetryTemplate paymentProviderWebhookConcurrencyRetryTemplate(
            PaymentProviderWebhookConcurrencyRetryProperties properties
    ) {

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
                properties.maxAttempts(),
                Map.of(
                        ConcurrentMerchantModificationException.class,
                        true
                )
        );

        UniformRandomBackOffPolicy backOffPolicy = new UniformRandomBackOffPolicy();
        backOffPolicy.setMinBackOffPeriod(properties.minBackoff().toMillis());
        backOffPolicy.setMaxBackOffPeriod(properties.maxBackoff().toMillis());

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }
}
