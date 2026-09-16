package com.dreamteam.alter.application.chat.event;

import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.common.notification.NotificationMessageConstants;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageBroadcaster;
import com.dreamteam.alter.domain.chat.port.outbound.ChatPresenceStore;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.notification.type.NotificationType;
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
    private final ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSent(ChatMessageSentEvent event) {
        ChatRoom chatRoom = event.getChatRoom();

        // 수신자 산정(findActiveByRoom)도 try 안에서 처리한다. AFTER_COMMIT 리스너의 예외는 커밋
        // 호출자에게 그대로 전파되므로(ALT-284에서 고정한 예외 누수 방지 보장), 이 조회가 실패하면
        // WS 전파와 FCM 폴백 둘 다 수신자 목록이 없어 어차피 못 하니 여기서 조용히 끝낸다.
        List<ChatRoomMember> activeMembers;
        try {
            // 수신자 산정은 발송 인스턴스에서 이 한 번만 한다 (WebSocket 배달 + FCM 폴백이 공유).
            activeMembers = chatRoomMemberQueryRepository.findActiveByRoom(chatRoom.getId());
        } catch (Exception e) {
            log.error("채팅 수신자 조회 실패. ChatRoomId: {}, Error: {}", chatRoom.getId(), e.getMessage(), e);
            return;
        }

        // 1. WebSocket으로 실시간 전송 (발신자 본인 포함, 활성 멤버 전원)
        try {
            List<String> recipientNames = activeMembers.stream()
                .map(member -> member.getMemberScope().principalName(member.getMemberId()))
                .toList();
            chatMessageBroadcaster.broadcast(chatRoom.getId(), event.getMessageResponse(), recipientNames);
        } catch (Exception e) {
            log.error("WebSocket 메시지 전송 실패. ChatRoomId: {}, Error: {}", chatRoom.getId(), e.getMessage(), e);
        }

        // 2. FCM 알림 전송 (활성 멤버 기준, 온라인 멤버는 제외한 presence 기반 폴백)
        // WebSocket 배달 성패와 무관하게 시도한다 (FCM은 실시간 배달 실패에 대한 폴백 경로이기도 하다).
        sendFcmNotification(
            chatRoom, activeMembers, event.getSenderId(), event.getSenderScope(),
            event.getContent(), event.getMessageResponse().getSenderName()
        );
    }

    private void sendFcmNotification(
        ChatRoom chatRoom,
        List<ChatRoomMember> activeMembers,
        Long senderId,
        TokenScope senderScope,
        String content,
        String senderName
    ) {
        try {
            // 발신자를 제외한 활성 멤버 추출
            List<ChatRoomMember> recipients = activeMembers.stream()
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
            String title = NotificationMessageConstants.Chat.NEW_MESSAGE_TITLE;
            String body = buildNotificationBody(senderName, content);
            notificationService.sendNotificationOnlyToMany(offlineUserIds, NotificationType.CHAT, title, body);

        } catch (Exception e) {
            // 알림 실패는 로그만 남기고 메시지 전송은 성공 처리
            log.error("채팅 메시지 FCM 알림 발송 실패. ChatRoomId: {}, Error: {}", chatRoom.getId(), e.getMessage(), e);
        }
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
