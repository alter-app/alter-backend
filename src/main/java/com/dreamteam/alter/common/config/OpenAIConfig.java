package com.dreamteam.alter.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder
            .defaultSystem("당신은 평판 데이터를 분석하여 간결하고 자연스러운 요약을 생성하는 전문가입니다.")
            .build();
    }
}
