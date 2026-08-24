package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.inbound.GetChatMessagesUseCase;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.result.ChatMessageResult;
import com.dreamteam.alter.domain.common.pagination.CursorPageQuery;
import com.dreamteam.alter.domain.common.pagination.CursorPageResult;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service("getChatMessages")
public class GetChatMessages extends AbstractGetChatMessagesUseCase<AppActor> implements GetChatMessagesUseCase {

    public GetChatMessages(
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
        AppActor actor,
        Long chatRoomId,
        CursorPageQuery query
    ) {
        return super.execute(actor, chatRoomId, query);
    }

    @Override
    protected TokenScope getParticipantScope(AppActor actor) {
        return super.getParticipantScope(actor);
    }

    @Override
    protected Long getParticipantId(AppActor actor) {
        return super.getParticipantId(actor);
    }
}
