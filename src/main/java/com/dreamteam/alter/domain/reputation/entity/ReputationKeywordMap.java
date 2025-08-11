package com.dreamteam.alter.domain.reputation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
    name = "reputation_keyword_map",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_reputation_keyword_map",
            columnNames = {"reputation_id", "keyword_id"}
        )
    }
)
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class ReputationKeywordMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @JoinColumn(name = "reputation_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Reputation reputation;

    @JoinColumn(name = "keyword_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ReputationKeyword keyword;

    @Column(name = "description")
    private String description;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static ReputationKeywordMap create(
        Reputation reputation,
        ReputationKeyword keyword,
        String description
    ) {
        return ReputationKeywordMap.builder()
            .reputation(reputation)
            .keyword(keyword)
            .description(description)
            .build();
    }

}
