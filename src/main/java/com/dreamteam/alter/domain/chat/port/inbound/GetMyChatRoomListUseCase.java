package com.dreamteam.alter.domain.chat.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.domain.chat.result.ChatRoomListResult;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetMyChatRoomListUseCase {
    CursorPaginatedApiResponse<ChatRoomListResult> execute(AppActor actor, CursorPageRequestDto pageRequest);
}
