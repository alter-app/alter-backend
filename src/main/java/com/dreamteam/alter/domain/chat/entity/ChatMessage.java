package com.dreamteam.alter.domain.chat.entity;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "chat_messages")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "chat_room_id", nullable = false)
    private Long chatRoomId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_scope", length = 20, nullable = false)
    private TokenScope senderScope;

    @Column(name = "content", length = 1000, nullable = false)
    private String content;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ChatMessage create(
        Long chatRoomId,
        Long senderId,
        TokenScope senderScope,
        String content
    ) {
        return ChatMessage.builder()
            .chatRoomId(chatRoomId)
            .senderId(senderId)
            .senderScope(senderScope)
            .content(content)
            .build();
    }

}
