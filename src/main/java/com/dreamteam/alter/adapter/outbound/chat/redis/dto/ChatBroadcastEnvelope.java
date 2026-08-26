package com.dreamteam.alter.adapter.outbound.chat.redis.dto;

import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ChatBroadcastEnvelope {

    private Long roomId;
    private ChatMessageResponse message;
    // 롤링 배포 중 구버전 인스턴스가 발행한(이 필드 없는) envelope을 신버전이 역직렬화할 때
    // null이 아니라 빈 리스트가 되도록 기본값을 둔다 - 없으면 ChatMessageRedisSubscriber의
    // for-each에서 NPE가 나 catch에 잡히고 그 메시지 전체가 조용히 유실된다.
    private List<String> recipientNames = List.of();

    public ChatBroadcastEnvelope(Long roomId, ChatMessageResponse message, List<String> recipientNames) {
        this.roomId = roomId;
        this.message = message;
        this.recipientNames = recipientNames;
    }
}
