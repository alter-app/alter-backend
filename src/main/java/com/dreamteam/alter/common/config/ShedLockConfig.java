package com.dreamteam.alter.common.config;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
@RequiredArgsConstructor
public class ShedLockConfig {

    private final Environment environment;

    @Bean
    public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
        String lockEnv = environment.getProperty("spring.profiles.active", "default");
        return new RedisLockProvider(connectionFactory, lockEnv);
    }

}
