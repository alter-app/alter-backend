package com.dreamteam.alter.adapter.outbound.aws.ses.config;

import com.dreamteam.alter.adapter.outbound.aws.ses.properties.AwsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.SesClientBuilder;

@Configuration
@RequiredArgsConstructor
public class AwsSesConfig {

    private final AwsProperties awsProperties;

    @Bean
    public SesClient sesClient() {
        SesClientBuilder builder = SesClient.builder()
                .region(Region.of(awsProperties.getRegion()));

        if (StringUtils.hasText(awsProperties.getAccessKey()) && StringUtils.hasText(awsProperties.getSecretKey())) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(
                    awsProperties.getAccessKey(),
                    awsProperties.getSecretKey()
            );
            builder.credentialsProvider(StaticCredentialsProvider.create(credentials));
        }

        return builder.build();
    }
}
