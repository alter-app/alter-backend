package com.dreamteam.alter.common.config;

import com.dreamteam.alter.adapter.outbound.chat.redis.ChatMessageRedisSubscriber;
import com.dreamteam.alter.adapter.outbound.chat.redis.RedisChatMessageBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class ChatRedisPubSubConfig {

    private final ChatMessageRedisSubscriber chatMessageRedisSubscriber;

    // Redis pub/sub 팬아웃의 필수 소비자. 현재 실시간 전파는 이 subscriber가 유일한 경로이므로
    // 기본 활성(matchIfMissing=true)이며 끄면 실시간 수신 자체가 동작하지 않는다.
    // 주의: 향후 RabbitMQ STOMP relay(공유 브로커)를 재도입하는 경우, 각 인스턴스가 relay로
    // 재발행하여 클라이언트가 메시지를 중복 수신하게 되므로 반드시 이 subscriber를
    // (chat.redis.subscriber.enabled=false 로) 비활성화해야 한다.
    @Bean
    @ConditionalOnProperty(
            name = "chat.redis.subscriber.enabled", havingValue = "true", matchIfMissing = true)
    public RedisMessageListenerContainer chatRedisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(chatMessageRedisSubscriber, new ChannelTopic(RedisChatMessageBroadcaster.CHANNEL));
        return container;
    }
}
