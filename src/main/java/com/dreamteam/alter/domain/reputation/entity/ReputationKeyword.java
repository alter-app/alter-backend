package com.dreamteam.alter.domain.reputation.entity;

import com.dreamteam.alter.domain.reputation.type.ReputationCategoryType;
import com.dreamteam.alter.domain.reputation.type.ReputationKeywordStatus;
import com.dreamteam.alter.domain.reputation.type.ReputationKeywordType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(name = "reputation_keywords")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class ReputationKeyword {

    @Id
    @Column(name = "id", length = 8, nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "keyword_type", length = 16, nullable = false)
    private ReputationKeywordType type;

    @Column(name = "emoji", length = 16, nullable = false)
    private String emoji;

    @Column(name = "description", length = 128, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 64, nullable = false)
    private ReputationCategoryType category;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private ReputationKeywordStatus status;

}
