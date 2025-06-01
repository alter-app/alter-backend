package com.dreamteam.alter.domain.workspace.entity;

import com.dreamteam.alter.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
    name = "workspace_shift_assignments",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_shift_assignments",
            columnNames = {"shift_id", "user_id"}
        )
    }
)
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class WorkspaceShiftAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "shift_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private WorkspaceShift shift;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static WorkspaceShiftAssignment create(WorkspaceShift shift, User user) {
        return WorkspaceShiftAssignment.builder()
            .shift(shift)
            .user(user)
            .build();
    }

}
