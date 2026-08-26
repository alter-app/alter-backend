package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.inbound.SendChatMessageUseCase;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomRepository;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.user.entity.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service("sendChatMessage")
public class SendChatMessage extends AbstractSendChatMessageUseCase<User> implements SendChatMessageUseCase {

    public SendChatMessage(
        ChatRoomQueryRepository chatRoomQueryRepository,
        ChatRoomRepository chatRoomRepository,
        ChatMessageRepository chatMessageRepository,
        AttachFilesUseCase attachFilesUseCase,
        FileQueryRepository fileQueryRepository,
        FileUrlService fileUrlService,
        ApplicationEventPublisher eventPublisher
    ) {
        super(
            chatRoomQueryRepository, chatRoomRepository, chatMessageRepository,
            attachFilesUseCase, fileQueryRepository, fileUrlService, eventPublisher
        );
    }

    @Override
    protected TokenScope getParticipantScope(User user) {
        return super.getParticipantScope(user);
    }

    @Override
    protected Long getParticipantId(User user) {
        return super.getParticipantId(user);
    }

    @Override
    protected String getParticipantName(User user) {
        return super.getParticipantName(user);
    }

}
