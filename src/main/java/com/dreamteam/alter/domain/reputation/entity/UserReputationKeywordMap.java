package com.dreamteam.alter.domain.reputation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(
    name = "user_reputation_keyword_map",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_user_reputation_keyword_map",
            columnNames = {"user_reputation_id", "keyword_id"}
        )
    }
)
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class UserReputationKeywordMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_reputation_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private UserReputation userReputation;

    @JoinColumn(name = "keyword_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ReputationKeyword keyword;

    public static UserReputationKeywordMap create(UserReputation userReputation, ReputationKeyword keyword) {
        return UserReputationKeywordMap.builder()
            .userReputation(userReputation)
            .keyword(keyword)
            .build();
    }

}
