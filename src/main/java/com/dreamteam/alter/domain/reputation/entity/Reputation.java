package com.dreamteam.alter.domain.reputation.entity;

import com.dreamteam.alter.domain.reputation.type.ReputationType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "reputations")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Reputation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "reputation_request_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ReputationRequest reputationRequest;

    @Column(name = "writer_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReputationType writerType;

    @Column(name = "writer_id", nullable = false)
    private Long writerId;

    @Column(name = "target_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReputationType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "workspace_id")
    private Long workspaceId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static Reputation create(ReputationRequest reputationRequest, ReputationType writerType, Long writerId, ReputationType targetType, Long targetId, Long workspaceId) {
        return Reputation.builder()
            .reputationRequest(reputationRequest)
            .writerType(writerType)
            .writerId(writerId)
            .targetType(targetType)
            .targetId(targetId)
            .workspaceId(workspaceId)
            .build();
    }
}
