package com.dreamteam.alter.application.chat.event;

import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 채팅 메시지 저장 완료 후 발행되는 이벤트.
 * 실시간 브로드캐스트/FCM 발송은 트랜잭션 커밋 이후(AFTER_COMMIT)에 처리되어,
 * 롤백 시 "유령 메시지" 전송을 막고 I/O를 DB 트랜잭션 밖으로 뺀다.
 */
@Getter
@AllArgsConstructor
public class ChatMessageSentEvent {
    private ChatRoom chatRoom;
    private Long senderId;
    private TokenScope senderScope;
    private String content;
    private ChatMessageResponse messageResponse;
}
