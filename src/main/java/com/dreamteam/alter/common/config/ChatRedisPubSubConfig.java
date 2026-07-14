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
