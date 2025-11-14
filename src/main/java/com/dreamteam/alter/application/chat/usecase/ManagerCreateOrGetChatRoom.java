package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.general.chat.dto.CreateChatRoomResponseDto;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.inbound.ManagerCreateOrGetChatRoomUseCase;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.port.outbound.ManagerUserQueryRepository;
import org.springframework.stereotype.Service;

@Service("managerCreateOrGetChatRoom")
public class ManagerCreateOrGetChatRoom extends AbstractCreateOrGetChatRoomUseCase<ManagerActor> implements
                                                                                                 ManagerCreateOrGetChatRoomUseCase {

    public ManagerCreateOrGetChatRoom(
        ChatRoomQueryRepository chatRoomQueryRepository,
        ChatRoomRepository chatRoomRepository,
        ManagerUserQueryRepository managerUserQueryRepository
    ) {
        super(chatRoomQueryRepository, chatRoomRepository, managerUserQueryRepository);
    }

    @Override
    public CreateChatRoomResponseDto execute(ManagerActor actor, Long opponentUserId, TokenScope opponentScope) {
        return super.execute(actor, opponentUserId, opponentScope);
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
