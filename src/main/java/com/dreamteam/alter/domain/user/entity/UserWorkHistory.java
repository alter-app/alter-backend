package com.dreamteam.alter.domain.user.entity;

import com.dreamteam.alter.domain.user.type.UserWorkHistoryType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "user_work_histories")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class UserWorkHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @JoinColumn(name = "workspace_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Workspace workspace;

    @Column(name = "external_workspace_name", length = 255, nullable = true)
    private String externalWorkspaceName;

    @Column(name = "position", length = 255, nullable = false)
    private String position;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private UserWorkHistoryType type;

    @Column(name = "description", length = Integer.MAX_VALUE, nullable = true)
    private String description;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static UserWorkHistory create(
        User user,
        Workspace workspace,
        String externalWorkspaceName,
        String position,
        LocalDate startDate,
        LocalDate endDate,
        UserWorkHistoryType type,
        String description
    ) {
        return UserWorkHistory.builder()
            .user(user)
            .workspace(workspace)
            .externalWorkspaceName(externalWorkspaceName)
            .position(position)
            .startDate(startDate)
            .endDate(endDate)
            .type(type)
            .description(description)
            .build();
    }

}
