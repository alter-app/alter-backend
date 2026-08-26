package com.dreamteam.alter.application.chat.event;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 채팅방 멤버십이 끊긴 유저의 WebSocket 세션을 강제 종료하기 위해 발행되는 이벤트.
 * 구독 인가(ChatSubscribeAuthorizationChannelInterceptor)는 SUBSCRIBE 시점 1회만 검사하므로,
 * 이미 연결된 세션은 멤버십이 끊긴 뒤에도 계속 메시지를 수신한다. 세션 자체를 닫아
 * 클라이언트가 재연결 후 구독을 다시 등록하도록 강제한다.
 * DB 커밋 이후(AFTER_COMMIT)에 전파되어야 하므로 리스너에서 그 시점을 보장한다.
 */
@Getter
@AllArgsConstructor
public class ChatSessionRevokeEvent {
    private TokenScope scope;
    private Long memberId;
    private Long roomId;
}
