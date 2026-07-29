package com.dreamteam.alter.application.chat.event;

import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.common.notification.NotificationMessageConstants;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageBroadcaster;
import com.dreamteam.alter.domain.chat.port.outbound.ChatPresenceStore;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import com.dreamteam.alter.domain.notification.type.NotificationType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Set;

/**
 * 채팅 메시지 커밋 이후 실시간 전송(Redis 브로드캐스트) + FCM 발송을 처리한다.
 * 메시지 저장 트랜잭션이 성공적으로 커밋된 뒤에만 실행된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageSentEventListener {

    private final ChatMessageBroadcaster chatMessageBroadcaster;
    private final ChatPresenceStore chatPresenceStore;
    private final NotificationService notificationService;
    private final UserQueryRepository userQueryRepository;
    private final ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSent(ChatMessageSentEvent event) {
        ChatRoom chatRoom = event.getChatRoom();

        // 1. WebSocket으로 실시간 전송
        try {
            chatMessageBroadcaster.broadcast(chatRoom.getId(), event.getMessageResponse());
        } catch (Exception e) {
            log.error("WebSocket 메시지 전송 실패. ChatRoomId: {}, Error: {}", chatRoom.getId(), e.getMessage(), e);
        }

        // 2. FCM 알림 전송 (온라인 멤버는 제외한 presence 기반 폴백)
        if (chatRoom.getType() == ChatRoomType.DIRECT) {
            sendDirectFcmNotification(chatRoom, event.getSenderId(), event.getSenderScope(), event.getContent());
        } else {
            sendGroupFcmNotification(chatRoom, event.getSenderId(), event.getSenderScope(), event.getContent());
        }
    }

    private void sendDirectFcmNotification(
        ChatRoom chatRoom,
        Long senderId,
        TokenScope senderScope,
        String content
    ) {
        try {
            // 상대방 정보 확인
            Long opponentId;
            TokenScope opponentScope;
            if (chatRoom.getParticipant1Id()
                .equals(senderId) && chatRoom.getParticipant1Scope()
                .equals(senderScope)) {
                opponentId = chatRoom.getParticipant2Id();
                opponentScope = chatRoom.getParticipant2Scope();
            } else {
                opponentId = chatRoom.getParticipant1Id();
                opponentScope = chatRoom.getParticipant1Scope();
            }

            // 상대방이 온라인이면 FCM 생략 (WebSocket으로 이미 수신)
            if (chatPresenceStore.isOnline(opponentScope, opponentId)) {
                return;
            }

            // 발신자 이름 조회
            String senderName = getSenderName(senderId);

            // 알림 메시지 생성
            String title = NotificationMessageConstants.Chat.NEW_MESSAGE_TITLE;
            String body = buildNotificationBody(senderName, content);

            // FCM 알림 전송
            notificationService.sendNotificationOnly(opponentId, NotificationType.CHAT, title, body);

        } catch (Exception e) {
            // 알림 실패는 로그만 남기고 메시지 전송은 성공 처리
            log.error("채팅 메시지 FCM 알림 발송 실패. ChatRoomId: {}, Error: {}", chatRoom.getId(), e.getMessage(), e);
        }
    }

    private void sendGroupFcmNotification(
        ChatRoom chatRoom,
        Long senderId,
        TokenScope senderScope,
        String content
    ) {
        try {
            // 발신자를 제외한 활성 멤버 추출
            List<ChatRoomMember> recipients = chatRoomMemberQueryRepository.findActiveByRoom(chatRoom.getId())
                .stream()
                .filter(member -> !(member.getMemberId().equals(senderId) && member.getMemberScope() == senderScope))
                .toList();
            if (recipients.isEmpty()) {
                return;
            }

            // presence 일괄 조회 후 온라인 멤버 제외(오프라인 멤버에게만 FCM 폴백)
            Set<ChatPresenceStore.PresenceTarget> onlineTargets = chatPresenceStore.filterOnline(
                recipients.stream()
                    .map(member -> new ChatPresenceStore.PresenceTarget(member.getMemberScope(), member.getMemberId()))
                    .toList()
            );
            List<Long> offlineUserIds = recipients.stream()
                .filter(member -> !onlineTargets.contains(
                    new ChatPresenceStore.PresenceTarget(member.getMemberScope(), member.getMemberId())))
                .map(ChatRoomMember::getMemberId)
                .toList();
            if (offlineUserIds.isEmpty()) {
                return;
            }

            // 알림 메시지 생성 후 배치 발송(토큰 조회·FCM 발송을 한 번에)
            String senderName = getSenderName(senderId);
            String title = NotificationMessageConstants.Chat.NEW_MESSAGE_TITLE;
            String body = buildNotificationBody(senderName, content);
            notificationService.sendNotificationOnlyToMany(offlineUserIds, NotificationType.CHAT, title, body);

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

    private String buildNotificationBody(String senderName, String content) {
        if (ObjectUtils.isEmpty(content)) {
            return String.format(NotificationMessageConstants.Chat.PHOTO_MESSAGE_BODY, senderName);
        }
        return String.format(NotificationMessageConstants.Chat.NEW_MESSAGE_BODY, senderName, truncateContent(content));
    }

    private String truncateContent(String content) {
        if (ObjectUtils.isEmpty(content)) {
            return "";
        }
        return content.length() > 50 ? content.substring(0, 50) + "..." : content;
    }
}
