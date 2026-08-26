package com.dreamteam.alter.common.constants;

public final class ChatConstants {

    /**
     * 채팅 메시지 유저 큐 브로커 prefix. {@code WebSocketConfig}의 {@code enableSimpleBroker}와
     * {@code CHAT_MESSAGE_USER_QUEUE_DESTINATION}이 반드시 같은 prefix를 공유해야 한다 —
     * 어긋나면 {@code convertAndSendToUser} 배달이 조용히 끊긴다.
     */
    public static final String CHAT_USER_QUEUE_PREFIX = "/queue";

    /**
     * 채팅 메시지 유저 큐 목적지. {@code ChatMessageRedisSubscriber}가
     * {@code convertAndSendToUser} 호출 시 사용한다.
     */
    public static final String CHAT_MESSAGE_USER_QUEUE_DESTINATION = CHAT_USER_QUEUE_PREFIX + "/chat.messages";

    private ChatConstants() {
    }
}
