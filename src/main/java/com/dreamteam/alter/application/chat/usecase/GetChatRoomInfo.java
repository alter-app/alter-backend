package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.inbound.GetChatRoomInfoUseCase;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import org.springframework.stereotype.Service;

@Service("getChatRoomInfo")
public class GetChatRoomInfo extends AbstractGetChatRoomUseCase<AppActor> implements GetChatRoomInfoUseCase {

    public GetChatRoomInfo(
        ChatRoomQueryRepository chatRoomQueryRepository,
        ChatRoomMemberQueryRepository chatRoomMemberQueryRepository,
        WorkspaceQueryRepository workspaceQueryRepository,
        UserQueryRepository userQueryRepository,
        FileUrlService fileUrlService
    ) {
        super(
            chatRoomQueryRepository,
            chatRoomMemberQueryRepository,
            workspaceQueryRepository,
            userQueryRepository,
            fileUrlService
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
