package com.dreamteam.alter.domain.reputation.entity;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "workspace_reputations")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class WorkspaceReputation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "request_id", nullable = false)
    @OneToOne(fetch = FetchType.LAZY)
    private ReputationRequest request;

    @JoinColumn(name = "writer_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @JoinColumn(name = "workspace_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Workspace workspace;

    @Column(name = "rating", nullable = false)
    private double rating; // decimal?

    @Column(name = "description", length = Integer.MAX_VALUE, nullable = false)
    private String description;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static WorkspaceReputation create(
        ReputationRequest request,
        User user,
        Workspace workspace,
        double rating,
        String description
    ) {
        return WorkspaceReputation.builder()
            .request(request)
            .user(user)
            .workspace(workspace)
            .rating(rating)
            .description(description)
            .build();
    }

}
