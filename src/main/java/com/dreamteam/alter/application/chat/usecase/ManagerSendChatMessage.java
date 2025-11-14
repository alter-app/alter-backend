package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.inbound.ManagerSendChatMessageUseCase;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomRepository;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service("managerSendChatMessage")
public class ManagerSendChatMessage extends AbstractSendChatMessageUseCase<ManagerUser> implements
                                                                                        ManagerSendChatMessageUseCase {

    public ManagerSendChatMessage(
        ChatRoomQueryRepository chatRoomQueryRepository,
        ChatRoomRepository chatRoomRepository,
        ChatMessageRepository chatMessageRepository,
        UserQueryRepository userQueryRepository,
        NotificationService notificationService,
        SimpMessagingTemplate messagingTemplate
    ) {
        super(
            chatRoomQueryRepository, chatRoomRepository, chatMessageRepository, userQueryRepository,
            notificationService, messagingTemplate
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
