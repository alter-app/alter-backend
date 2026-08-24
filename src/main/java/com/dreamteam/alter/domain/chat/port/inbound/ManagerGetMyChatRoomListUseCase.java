package com.dreamteam.alter.domain.chat.port.inbound;

import com.dreamteam.alter.domain.chat.result.ChatRoomListResult;
import com.dreamteam.alter.domain.common.pagination.CursorPageQuery;
import com.dreamteam.alter.domain.common.pagination.CursorPageResult;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetMyChatRoomListUseCase {
    CursorPageResult<ChatRoomListResult> execute(ManagerActor actor, CursorPageQuery query);
}
