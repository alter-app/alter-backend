package com.dreamteam.alter.domain.chat.entity;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

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

    @Column(name = "participant1_id")
    private Long participant1Id;

    @Enumerated(EnumType.STRING)
    @Column(name = "participant1_scope", length = 20)
    private TokenScope participant1Scope;

    @Column(name = "participant2_id")
    private Long participant2Id;

    @Enumerated(EnumType.STRING)
    @Column(name = "participant2_scope", length = 20)
    private TokenScope participant2Scope;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private ChatRoomType type;

    @Column(name = "workspace_id")
    private Long workspaceId;

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
            .type(ChatRoomType.DIRECT)
            .build();
    }

    public static ChatRoom createGroup(Long workspaceId) {
        // GROUP 방은 반드시 업장과 연관되어야 한다 (도메인 불변식)
        if (workspaceId == null) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "GROUP 채팅방은 workspaceId가 필수입니다.");
        }
        return ChatRoom.builder()
            .type(ChatRoomType.GROUP)
            .workspaceId(workspaceId)
            // participant 컬럼은 DIRECT 전용: 그룹은 멤버 테이블로 관리하므로 사용 안 함
            .build();
    }

    public void updateUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    // DIRECT 방의 참여자 여부(participant 컬럼 기반). 이미 로드된 방으로 검증해 재조회를 피한다.
    public boolean isParticipant(Long memberId, TokenScope scope) {
        return (Objects.equals(participant1Id, memberId) && participant1Scope == scope)
            || (Objects.equals(participant2Id, memberId) && participant2Scope == scope);
    }
}
