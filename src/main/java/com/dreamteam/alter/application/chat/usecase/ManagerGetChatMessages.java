package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.inbound.ManagerGetChatMessagesUseCase;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.result.ChatMessageResult;
import com.dreamteam.alter.domain.common.pagination.CursorPageQuery;
import com.dreamteam.alter.domain.common.pagination.CursorPageResult;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service("managerGetChatMessages")
public class ManagerGetChatMessages extends AbstractGetChatMessagesUseCase<ManagerActor> implements
                                                                                         ManagerGetChatMessagesUseCase {

    public ManagerGetChatMessages(
        ChatRoomQueryRepository chatRoomQueryRepository,
        ChatMessageQueryRepository chatMessageQueryRepository,
        ObjectMapper objectMapper,
        ChatRoomMemberQueryRepository chatRoomMemberQueryRepository,
        FileQueryRepository fileQueryRepository,
        FileUrlService fileUrlService
    ) {
        super(
            chatRoomQueryRepository,
            chatMessageQueryRepository,
            objectMapper,
            chatRoomMemberQueryRepository,
            fileQueryRepository,
            fileUrlService
        );
    }

    @Override
    public CursorPageResult<ChatMessageResult> execute(
        ManagerActor actor,
        Long chatRoomId,
        CursorPageQuery query
    ) {
        return super.execute(actor, chatRoomId, query);
    }

    @Override
    protected TokenScope getParticipantScope(ManagerActor actor) {
        return super.getParticipantScope(actor);
    }

    @Override
    protected Long getParticipantId(ManagerActor actor) {
        return super.getParticipantId(actor);
    }
}
