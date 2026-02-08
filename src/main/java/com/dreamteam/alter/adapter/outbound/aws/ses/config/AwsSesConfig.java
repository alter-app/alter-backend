package com.dreamteam.alter.adapter.outbound.aws.ses.config;

import com.dreamteam.alter.adapter.outbound.aws.ses.properties.AwsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
@RequiredArgsConstructor
public class AwsSesConfig {

    private final AwsProperties awsProperties;

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .build();
    }
}
