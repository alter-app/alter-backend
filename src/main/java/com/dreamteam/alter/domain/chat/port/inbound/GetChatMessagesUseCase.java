package com.dreamteam.alter.domain.chat.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatMessageResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetChatMessagesUseCase {
    CursorPaginatedApiResponse<ChatMessageResponseDto> execute(AppActor actor, Long chatRoomId, CursorPageRequestDto pageRequest);
}
