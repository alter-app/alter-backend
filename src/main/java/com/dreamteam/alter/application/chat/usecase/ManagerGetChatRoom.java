package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.inbound.ManagerGetChatRoomUseCase;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import org.springframework.stereotype.Service;

@Service("managerGetChatRoom")
public class ManagerGetChatRoom extends AbstractGetChatRoomUseCase<ManagerActor> implements ManagerGetChatRoomUseCase {

    public ManagerGetChatRoom(
        ChatRoomQueryRepository chatRoomQueryRepository,
        ChatRoomMemberQueryRepository chatRoomMemberQueryRepository,
        WorkspaceQueryRepository workspaceQueryRepository,
        UserQueryRepository userQueryRepository,
        FileQueryRepository fileQueryRepository,
        FileUrlService fileUrlService
    ) {
        super(
            chatRoomQueryRepository,
            chatRoomMemberQueryRepository,
            workspaceQueryRepository,
            userQueryRepository,
            fileQueryRepository,
            fileUrlService
        );
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
