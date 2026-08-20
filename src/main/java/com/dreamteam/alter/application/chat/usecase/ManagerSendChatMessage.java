package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.inbound.ManagerSendChatMessageUseCase;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomRepository;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service("managerSendChatMessage")
public class ManagerSendChatMessage extends AbstractSendChatMessageUseCase<ManagerUser> implements
                                                                                        ManagerSendChatMessageUseCase {

    public ManagerSendChatMessage(
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
    protected TokenScope getParticipantScope(ManagerUser managerUser) {
        return super.getParticipantScope(managerUser);
    }

    @Override
    protected Long getParticipantId(ManagerUser managerUser) {
        return super.getParticipantId(managerUser);
    }

}
