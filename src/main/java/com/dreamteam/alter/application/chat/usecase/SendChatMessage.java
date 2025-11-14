package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.inbound.SendChatMessageUseCase;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomRepository;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service("sendChatMessage")
public class SendChatMessage extends AbstractSendChatMessageUseCase<User> implements SendChatMessageUseCase {

    public SendChatMessage(
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
    protected TokenScope getParticipantScope(User user) {
        return super.getParticipantScope(user);
    }

    @Override
    protected Long getParticipantId(User user) {
        return super.getParticipantId(user);
    }

}
