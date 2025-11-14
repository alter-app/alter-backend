package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.general.chat.dto.SendChatMessageRequestDto;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.notification.NotificationMessageConstants;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatMessage;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomRepository;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional
public abstract class AbstractSendChatMessageUseCase<U> extends AbstractChatUseCase {

    protected final ChatRoomQueryRepository chatRoomQueryRepository;
    protected final ChatRoomRepository chatRoomRepository;
    protected final ChatMessageRepository chatMessageRepository;
    protected final UserQueryRepository userQueryRepository;
    protected final NotificationService notificationService;
    protected final SimpMessagingTemplate messagingTemplate;

    public final void execute(U user, SendChatMessageRequestDto request, Long chatRoomId) {
        TokenScope senderScope = getParticipantScope(user);
        Long senderId = getParticipantId(user);

        // 1. 채팅방 존재 확인 및 참여자 검증
        ChatRoom chatRoom = chatRoomQueryRepository.findByIdAndParticipant(
                chatRoomId,
                senderId,
                senderScope
            )
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        // 2. 메시지 저장
        ChatMessage chatMessage = ChatMessage.create(
            chatRoom.getId(),
            senderId,
            senderScope,
            request.getContent()
        );
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // 3. ChatRoom의 updatedAt 갱신
        chatRoom.updateUpdatedAt();

        // 4. WebSocket으로 실시간 전송
        try {
            ChatMessageResponse messageResponse = new ChatMessageResponse(
                savedMessage.getId(),
                savedMessage.getChatRoomId(),
                savedMessage.getSenderId(),
                savedMessage.getSenderScope(),
                savedMessage.getContent(),
                savedMessage.getCreatedAt()
            );
            messagingTemplate.convertAndSend("/sub/chat." + chatRoom.getId(), messageResponse);
        } catch (Exception e) {
            log.error("WebSocket 메시지 전송 실패. ChatRoomId: {}, Error: {}", chatRoom.getId(), e.getMessage(), e);
        }

        // 5. 상대방에게 FCM 알림 전송
        sendFcmNotification(chatRoom, senderId, senderScope, request.getContent());
    }

    private void sendFcmNotification(
        ChatRoom chatRoom,
        Long senderId,
        TokenScope senderScope,
        String content
    ) {
        try {
            // 상대방 정보 확인
            Long opponentId;
            if (chatRoom.getParticipant1Id()
                .equals(senderId) && chatRoom.getParticipant1Scope()
                .equals(senderScope)) {
                opponentId = chatRoom.getParticipant2Id();
            } else {
                opponentId = chatRoom.getParticipant1Id();
            }

            // 발신자 이름 조회
            String senderName = getSenderName(senderId);

            // 알림 메시지 생성
            String title = NotificationMessageConstants.Chat.NEW_MESSAGE_TITLE;
            String body = String.format(
                NotificationMessageConstants.Chat.NEW_MESSAGE_BODY,
                senderName,
                truncateContent(content)
            );

            // FCM 알림 전송
            notificationService.sendNotificationOnly(opponentId, title, body);

        } catch (Exception e) {
            // 알림 실패는 로그만 남기고 메시지 전송은 성공 처리
            log.error("채팅 메시지 FCM 알림 발송 실패. ChatRoomId: {}, Error: {}", chatRoom.getId(), e.getMessage(), e);
        }
    }

    private String getSenderName(Long senderId) {
        return userQueryRepository.findById(senderId)
            .map(User::getName)
            .orElse("알 수 없음");
    }

    private String truncateContent(String content) {
        if (ObjectUtils.isEmpty(content)) {
            return "";
        }
        return content.length() > 50 ? content.substring(0, 50) + "..." : content;
    }

    protected abstract TokenScope getParticipantScope(U user);

    protected abstract Long getParticipantId(U user);

}
