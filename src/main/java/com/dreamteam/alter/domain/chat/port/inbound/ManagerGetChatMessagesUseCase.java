package com.dreamteam.alter.domain.chat.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.domain.chat.result.ChatMessageResult;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetChatMessagesUseCase {
    CursorPaginatedApiResponse<ChatMessageResult> execute(ManagerActor actor, Long chatRoomId, CursorPageRequestDto pageRequest);
}
