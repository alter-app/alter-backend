package com.dreamteam.alter.adapter.outbound.aws.ses.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {
    private String region;
    private String accessKey;
    private String secretKey;
}
