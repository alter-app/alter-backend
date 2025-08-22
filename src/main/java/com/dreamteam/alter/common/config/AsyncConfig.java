package com.dreamteam.alter.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "batchTaskExecutor")
    public Executor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);    // 기본 스레드 수: 10개
        executor.setMaxPoolSize(4);     // 최대 스레드 수: 20개
        executor.setQueueCapacity(50);  // 대기 큐 크기: 100개
        executor.setThreadNamePrefix("batch-"); // 스레드 이름 접두사
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(60); // 유휴 스레드 유지 시간: 60초
        executor.initialize();
        return executor;
    }

    /**
     * 배치 작업 설정 상수
     */
    public static class BatchConfig {
        public static final int BATCH_SIZE = 50;           // 한 번에 처리할 배치 크기
        public static final int MAX_CONCURRENT_BATCHES = 2; // 동시 실행할 배치 수
        public static final int TIMEOUT_SECONDS = 300;     // 배치 작업 타임아웃 (5분)
    }
}
