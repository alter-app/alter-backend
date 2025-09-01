package com.dreamteam.alter.domain.reputation.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.dreamteam.alter.adapter.inbound.common.dto.reputation.KeywordSummaryDto;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = ReputationSummary.TABLE_NAME)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class ReputationSummary {

    public static final String TABLE_NAME = "reputation_summary";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TARGET_TYPE = "target_type";
    public static final String COLUMN_TARGET_ID = "target_id";
    public static final String COLUMN_TOTAL_REPUTATION_COUNT = "total_reputation_count";
    public static final String COLUMN_TOP_KEYWORDS = "top_keywords";
    public static final String COLUMN_SUMMARY_DESCRIPTION = "summary_description";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = COLUMN_ID)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = COLUMN_TARGET_TYPE, nullable = false)
    private ReputationType targetType;

    @Column(name = COLUMN_TARGET_ID, nullable = false)
    private Long targetId;

    @Column(name = COLUMN_TOTAL_REPUTATION_COUNT, nullable = false)
    private Integer totalReputationCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = COLUMN_TOP_KEYWORDS, columnDefinition = "jsonb")
    private List<KeywordSummaryDto> topKeywords;

    @Column(name = COLUMN_SUMMARY_DESCRIPTION)
    private String summaryDescription;

    @CreatedDate
    @Column(name = COLUMN_CREATED_AT, nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = COLUMN_UPDATED_AT, nullable = false)
    private LocalDateTime updatedAt;

    public static ReputationSummary create(
        ReputationType targetType,
        Long targetId,
        Integer totalReputationCount,
        List<KeywordSummaryDto> topKeywords
    ) {
        return ReputationSummary.builder()
            .targetType(targetType)
            .targetId(targetId)
            .totalReputationCount(totalReputationCount)
            .topKeywords(topKeywords)
            .build();
    }

    public void updateSummary(
        Integer totalReputationCount,
        List<KeywordSummaryDto> topKeywords
    ) {
        this.totalReputationCount = totalReputationCount;
        this.topKeywords = topKeywords;
    }

    public void updateSummaryDescription(String summaryDescription) {
        this.summaryDescription = summaryDescription;
    }

}
