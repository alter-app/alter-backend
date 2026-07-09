package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.inbound.SendChatMessageUseCase;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatPresenceStore;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomRepository;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
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
        SimpMessagingTemplate messagingTemplate,
        ChatRoomMemberQueryRepository chatRoomMemberQueryRepository,
        ChatPresenceStore chatPresenceStore,
        AttachFilesUseCase attachFilesUseCase,
        FileQueryRepository fileQueryRepository,
        FileUrlService fileUrlService
    ) {
        super(
            chatRoomQueryRepository, chatRoomRepository, chatMessageRepository, userQueryRepository,
            notificationService, messagingTemplate, chatRoomMemberQueryRepository, chatPresenceStore,
            attachFilesUseCase, fileQueryRepository, fileUrlService
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
