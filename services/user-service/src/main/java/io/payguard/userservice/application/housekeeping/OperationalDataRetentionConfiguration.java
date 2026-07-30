package io.payguard.userservice.application.housekeeping;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OperationalDataRetentionProperties.class)
public class OperationalDataRetentionConfiguration {
}
