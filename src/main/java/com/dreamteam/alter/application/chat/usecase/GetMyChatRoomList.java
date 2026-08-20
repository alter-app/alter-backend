package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.inbound.GetMyChatRoomListUseCase;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service("getMyChatRoomList")
public class GetMyChatRoomList extends AbstractGetMyChatRoomListUseCase<AppActor> implements GetMyChatRoomListUseCase {

    public GetMyChatRoomList(
        ChatRoomQueryRepository chatRoomQueryRepository,
        ChatMessageQueryRepository chatMessageQueryRepository,
        ChatRoomMemberQueryRepository chatRoomMemberQueryRepository,
        FileQueryRepository fileQueryRepository,
        FileUrlService fileUrlService,
        ObjectMapper objectMapper
    ) {
        super(
            chatRoomQueryRepository,
            chatMessageQueryRepository,
            chatRoomMemberQueryRepository,
            fileQueryRepository,
            fileUrlService,
            objectMapper
        );
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
