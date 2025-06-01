package com.dreamteam.alter.domain.reputation.entity;

import com.dreamteam.alter.domain.reputation.type.UserReputationType;
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
@Table(name = "user_reputations")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class UserReputation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "request_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ReputationRequest request;

    @JoinColumn(name = "writer_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User writer;

    @JoinColumn(name = "target_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User target;

    @JoinColumn(name = "workspace_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Workspace workspace;

    @Column(name = "rating", nullable = false)
    private double rating;

    @Column(name = "description", length = Integer.MAX_VALUE, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private UserReputationType type;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static UserReputation create(
        ReputationRequest request,
        User writer,
        User target,
        Workspace workspace,
        double rating,
        String description,
        UserReputationType type
    ) {
        return UserReputation.builder()
            .request(request)
            .writer(writer)
            .target(target)
            .workspace(workspace)
            .rating(rating)
            .description(description)
            .type(type)
            .build();
    }

}
