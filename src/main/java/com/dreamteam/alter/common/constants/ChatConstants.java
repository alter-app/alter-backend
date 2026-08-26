package com.dreamteam.alter.common.constants;

public final class ChatConstants {

    /**
     * 채팅방 메시지 STOMP 구독 destination prefix. {@code prefix + roomId} 형태로 쓰인다.
     * 발행측({@code ChatMessageRedisSubscriber})과 구독 인가측
     * ({@code ChatSubscribeAuthorizationChannelInterceptor})이 반드시 같은 값을 참조해야 한다 —
     * 어긋나면 인가 검사가 fail-open으로 조용히 뚫린다.
     */
    public static final String CHAT_SUBSCRIBE_DESTINATION_PREFIX = "/sub/chat.";

    private ChatConstants() {
    }
}
