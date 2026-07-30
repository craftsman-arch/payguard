package io.payguard.userservice.integration.event.outbox;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({
        OutboxPublisherProperties.class,
        OutboxObservabilityProperties.class
})
public class OutboxPublisherConfiguration {

    @Bean
    @ConditionalOnProperty(
            prefix = "outbox.publisher",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public NewTopic merchantEventsTopic(
            OutboxPublisherProperties properties
    ) {

        return TopicBuilder
                .name(properties.topic())
                .partitions(properties.topicPartitions())
                .replicas(properties.topicReplicas())
                .build();
    }
}
