package com.dreamteam.alter.domain.chat.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.domain.chat.result.ChatRoomListResult;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetMyChatRoomListUseCase {
    CursorPaginatedApiResponse<ChatRoomListResult> execute(ManagerActor actor, CursorPageRequestDto pageRequest);
}
