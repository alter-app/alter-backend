package com.dreamteam.alter.domain.chat.port.inbound;

import com.dreamteam.alter.domain.chat.result.ChatRoomListResult;
import com.dreamteam.alter.domain.common.pagination.CursorPageQuery;
import com.dreamteam.alter.domain.common.pagination.CursorPageResult;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetMyChatRoomListUseCase {
    CursorPageResult<ChatRoomListResult> execute(AppActor actor, CursorPageQuery query);
}
