package com.dreamteam.alter.adapter.outbound.chat.redis;

import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;
import com.dreamteam.alter.adapter.outbound.chat.redis.dto.ChatBroadcastEnvelope;
import com.dreamteam.alter.common.constants.ChatConstants;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.type.ChatMessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMessageRedisSubscriber 테스트")
class ChatMessageRedisSubscriberTests {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private DefaultMessage redisMessage(ChatBroadcastEnvelope envelope) throws Exception {
        String payload = objectMapper.writeValueAsString(envelope);
        return new DefaultMessage("chat:broadcast".getBytes(StandardCharsets.UTF_8),
            payload.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("엔벌로프의 수신자 각각에게 유저 큐로 배달한다(발신자 본인 포함)")
    void onMessage_정상_유저큐로_배달() throws Exception {
        // given
        ChatMessageRedisSubscriber sut = new ChatMessageRedisSubscriber(messagingTemplate, objectMapper);
        ChatMessageResponse message = new ChatMessageResponse(
            10L, 100L, 1L, TokenScope.APP, "홍길동", ChatMessageType.NORMAL, "안녕하세요", LocalDateTime.of(2026, 7, 14, 12, 0, 0)
        );
        ChatBroadcastEnvelope envelope = new ChatBroadcastEnvelope(100L, message, List.of("APP:1", "APP:2"));

        // when
        sut.onMessage(redisMessage(envelope), null);

        // then
        then(messagingTemplate).should()
            .convertAndSendToUser(eq("APP:1"), eq(ChatConstants.CHAT_MESSAGE_USER_QUEUE_DESTINATION), any(ChatMessageResponse.class));
        then(messagingTemplate).should()
            .convertAndSendToUser(eq("APP:2"), eq(ChatConstants.CHAT_MESSAGE_USER_QUEUE_DESTINATION), any(ChatMessageResponse.class));
        then(messagingTemplate).should(times(2))
            .convertAndSendToUser(any(String.class), any(String.class), any(ChatMessageResponse.class));
    }

    @Test
    @DisplayName("배달 목적지가 브로커에 등록된 유저 큐 prefix로 시작한다(공유 상수 fail-open 방지)")
    void onMessage_배달목적지가_공유prefix로_시작한다() throws Exception {
        // given
        ChatMessageRedisSubscriber sut = new ChatMessageRedisSubscriber(messagingTemplate, objectMapper);
        ChatMessageResponse message = new ChatMessageResponse(
            10L, 100L, 1L, TokenScope.APP, "홍길동", ChatMessageType.NORMAL, "안녕하세요", LocalDateTime.of(2026, 7, 14, 12, 0, 0)
        );
        ChatBroadcastEnvelope envelope = new ChatBroadcastEnvelope(100L, message, List.of("APP:1"));

        // when
        sut.onMessage(redisMessage(envelope), null);

        // then
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        then(messagingTemplate).should()
            .convertAndSendToUser(eq("APP:1"), destinationCaptor.capture(), any(ChatMessageResponse.class));
        assertThat(destinationCaptor.getValue()).startsWith(ChatConstants.CHAT_USER_QUEUE_PREFIX);
    }

    @Test
    @DisplayName("recipientNames 필드가 없는(구버전이 발행한) envelope도 NPE 없이 처리한다")
    void onMessage_recipientNames_필드없음_NPE없이_처리() {
        // given: 롤링 배포 중 구버전 인스턴스가 recipientNames 필드 자체가 없는 JSON을 발행했다고 가정
        ChatMessageRedisSubscriber sut = new ChatMessageRedisSubscriber(messagingTemplate, objectMapper);
        String payload = "{\"roomId\":100,"
            + "\"message\":{\"id\":10,\"chatRoomId\":100,\"senderId\":1,\"senderScope\":\"APP\","
            + "\"type\":\"NORMAL\",\"content\":\"안녕하세요\",\"createdAt\":\"2026-07-14T12:00:00\"}}";
        DefaultMessage redisMessage = new DefaultMessage("chat:broadcast".getBytes(StandardCharsets.UTF_8),
            payload.getBytes(StandardCharsets.UTF_8));

        // when & then: for-each가 NPE 없이 그냥 아무도 배달하지 않고 끝나야 한다.
        sut.onMessage(redisMessage, null);
        then(messagingTemplate).should(never())
            .convertAndSendToUser(any(String.class), any(String.class), any(Object.class));
    }

    @Test
    @DisplayName("역직렬화 불가능한 메시지는 예외 없이 skip하며 배달하지 않는다")
    void onMessage_잘못된_JSON_skip() {
        // given
        ChatMessageRedisSubscriber sut = new ChatMessageRedisSubscriber(messagingTemplate, objectMapper);
        DefaultMessage redisMessage = new DefaultMessage("chat:broadcast".getBytes(StandardCharsets.UTF_8),
            "not-a-json".getBytes(StandardCharsets.UTF_8));

        // when
        sut.onMessage(redisMessage, null);

        // then
        then(messagingTemplate).should(never())
            .convertAndSendToUser(any(String.class), any(String.class), any(Object.class));
    }
}
