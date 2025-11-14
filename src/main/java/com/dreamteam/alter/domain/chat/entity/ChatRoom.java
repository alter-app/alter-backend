package com.dreamteam.alter.domain.chat.entity;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "chat_rooms")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "participant1_id", nullable = false)
    private Long participant1Id;

    @Enumerated(EnumType.STRING)
    @Column(name = "participant1_scope", length = 20, nullable = false)
    private TokenScope participant1Scope;

    @Column(name = "participant2_id", nullable = false)
    private Long participant2Id;

    @Enumerated(EnumType.STRING)
    @Column(name = "participant2_scope", length = 20, nullable = false)
    private TokenScope participant2Scope;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static ChatRoom create(
        Long participant1Id,
        TokenScope participant1Scope,
        Long participant2Id,
        TokenScope participant2Scope
    ) {
        return ChatRoom.builder()
            .participant1Id(participant1Id)
            .participant1Scope(participant1Scope)
            .participant2Id(participant2Id)
            .participant2Scope(participant2Scope)
            .build();
    }

    public void updateUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }
}
