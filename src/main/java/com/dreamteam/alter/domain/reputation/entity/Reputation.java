package com.dreamteam.alter.domain.reputation.entity;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.ReputationKeywordMapDto;
import com.dreamteam.alter.domain.reputation.type.ReputationStatus;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    @JoinColumn(name = "workspace_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Workspace workspace;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ReputationStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "reputation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ReputationKeywordMap> reputationKeywordMaps;

    public static Reputation create(ReputationRequest reputationRequest, ReputationType writerType, Long writerId, ReputationType targetType, Long targetId, Workspace workspace) {
        return Reputation.builder()
            .reputationRequest(reputationRequest)
            .writerType(writerType)
            .writerId(writerId)
            .targetType(targetType)
            .targetId(targetId)
            .workspace(workspace)
            .status(ReputationStatus.REQUESTED)
            .build();
    }

    public void addReputationKeywordMap(
        Set<ReputationKeywordMapDto> keywords,
        Map<String, ReputationKeyword> keywordMap
    ) {
        if (ObjectUtils.isEmpty(this.reputationKeywordMaps)) {
            this.reputationKeywordMaps = Set.of();
        }

        this.reputationKeywordMaps = keywords.stream()
            .map(
                keyword ->
                    ReputationKeywordMap.create(
                        this,
                        keywordMap.get(keyword.getKeywordId()),
                        keyword.getDescription()
                    )
            )
            .collect(Collectors.toSet());
    }

    public void decline() {
        this.status = ReputationStatus.DECLINED;
    }

    public void complete() {
        this.status = ReputationStatus.COMPLETED;
    }

    public void cancel() {
        this.status = ReputationStatus.CANCELED;
    }

}
