package com.dreamteam.alter.domain.chat.port.inbound;

import com.dreamteam.alter.domain.chat.result.ChatMessageResult;
import com.dreamteam.alter.domain.common.pagination.CursorPageQuery;
import com.dreamteam.alter.domain.common.pagination.CursorPageResult;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetChatMessagesUseCase {
    CursorPageResult<ChatMessageResult> execute(ManagerActor actor, Long chatRoomId, CursorPageQuery query);
}
