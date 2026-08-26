package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.inbound.CreateOrGetChatRoomUseCase;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomRepository;
import com.dreamteam.alter.domain.chat.result.CreateChatRoomResult;
import com.dreamteam.alter.domain.user.context.AppActor;
import org.springframework.stereotype.Service;

@Service("createOrGetChatRoom")
public class CreateOrGetChatRoom extends AbstractCreateOrGetChatRoomUseCase<AppActor> implements
                                                                                      CreateOrGetChatRoomUseCase {

    public CreateOrGetChatRoom(
        ChatRoomQueryRepository chatRoomQueryRepository,
        ChatRoomRepository chatRoomRepository,
        ChatRoomMemberRepository chatRoomMemberRepository,
        ChatRoomMemberQueryRepository chatRoomMemberQueryRepository,
        ChatMessageQueryRepository chatMessageQueryRepository
    ) {
        super(chatRoomQueryRepository, chatRoomRepository, chatRoomMemberRepository, chatRoomMemberQueryRepository,
            chatMessageQueryRepository);
    }

    @Override
    public CreateChatRoomResult execute(AppActor actor, Long opponentUserId, TokenScope opponentScope) {
        return super.execute(actor, opponentUserId, opponentScope);
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
