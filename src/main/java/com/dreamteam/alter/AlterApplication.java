package com.dreamteam.alter;

import com.dreamteam.alter.adapter.outbound.aws.ses.properties.AwsProperties;
import com.dreamteam.alter.application.email.properties.EmailAuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableJpaAuditing
@EnableRetry
@EnableConfigurationProperties({EmailAuthProperties.class, AwsProperties.class})
public class AlterApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlterApplication.class, args);
	}

}
