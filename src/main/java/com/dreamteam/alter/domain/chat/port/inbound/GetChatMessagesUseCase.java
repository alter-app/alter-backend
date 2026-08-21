package com.dreamteam.alter.domain.chat.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.domain.chat.result.ChatMessageResult;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetChatMessagesUseCase {
    CursorPaginatedApiResponse<ChatMessageResult> execute(AppActor actor, Long chatRoomId, CursorPageRequestDto pageRequest);
}
