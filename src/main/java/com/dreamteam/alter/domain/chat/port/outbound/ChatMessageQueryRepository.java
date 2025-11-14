package com.dreamteam.alter.domain.chat.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;

import java.util.List;
import java.util.Map;

public interface ChatMessageQueryRepository {
    List<ChatMessageResponse> getChatMessagesWithCursor(
        Long chatRoomId,
        CursorPageRequest<CursorDto> pageRequest
    );

    /**
     * 여러 채팅방의 최신 메시지 내용을 조회
     * @param chatRoomIds 채팅방 ID 목록
     * @return 채팅방 ID를 키로, 최신 메시지 내용을 값으로 하는 Map
     */
    Map<Long, String> getLatestMessageContentsByChatRoomIds(List<Long> chatRoomIds);
}
