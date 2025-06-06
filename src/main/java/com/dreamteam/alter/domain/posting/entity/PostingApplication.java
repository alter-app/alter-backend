package com.dreamteam.alter.domain.posting.entity;

import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import com.dreamteam.alter.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "posting_applications")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class PostingApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "posting_schedule_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PostingSchedule postingSchedule;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @Column(name = "description", length = Integer.MAX_VALUE, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @SQLRestriction("status != 'DELETED'")
    @Column(name = "status", nullable = false)
    private PostingApplicationStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static PostingApplication create(
        PostingSchedule postingSchedule,
        User user,
        String description
    ) {
        return PostingApplication.builder()
            .postingSchedule(postingSchedule)
            .user(user)
            .description(description)
            .status(PostingApplicationStatus.SUBMITTED)
            .build();
    }

    public void updateStatus(PostingApplicationStatus status) {
        this.status = status;
    }

}
