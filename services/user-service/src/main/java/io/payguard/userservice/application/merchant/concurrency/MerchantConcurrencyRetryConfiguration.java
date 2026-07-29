package io.payguard.userservice.application.merchant.concurrency;

import io.payguard.userservice.domain.merchant.exception.ConcurrentMerchantModificationException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.UniformRandomBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Map;

@Configuration
@EnableConfigurationProperties(MerchantConcurrencyRetryProperties.class)
public class MerchantConcurrencyRetryConfiguration {

    public static final String RETRY_TEMPLATE =
            "merchantConcurrencyRetryTemplate";

    @Bean(RETRY_TEMPLATE)
    public RetryTemplate merchantConcurrencyRetryTemplate(
            MerchantConcurrencyRetryProperties properties
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
