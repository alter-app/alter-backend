package com.dreamteam.alter.domain.reputation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(
    name = "workspace_reputation_keyword_map",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_workspace_reputation_keyword_map",
            columnNames = {"workspace_reputation_id", "keyword_id"}
        )
    }
)
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class WorkspaceReputationKeywordMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "workspace_reputation_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private WorkspaceReputation workspaceReputation;

    @JoinColumn(name = "keyword_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ReputationKeyword keyword;

    public static WorkspaceReputationKeywordMap create(WorkspaceReputation workspaceReputation, ReputationKeyword keyword) {
        return WorkspaceReputationKeywordMap.builder()
            .workspaceReputation(workspaceReputation)
            .keyword(keyword)
            .build();
    }

}
