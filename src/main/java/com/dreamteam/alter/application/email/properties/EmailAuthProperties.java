package com.dreamteam.alter.application.email.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "alter.email")
public class EmailAuthProperties {
    private String from;
    private long codeTtlSeconds = 300;
    private long verifiedTtlSeconds = 900;
    private long cooldownSeconds = 30;
}
