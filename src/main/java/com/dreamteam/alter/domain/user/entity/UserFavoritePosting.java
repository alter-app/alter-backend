package com.dreamteam.alter.domain.user.entity;

import com.dreamteam.alter.domain.posting.entity.Posting;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
    name = "user_favorite_postings",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "posting_id"})
)
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class UserFavoritePosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "posting_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Posting posting;

    @LastModifiedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static UserFavoritePosting create(User user, Posting posting) {
        return UserFavoritePosting.builder()
            .user(user)
            .posting(posting)
            .build();
    }

}
