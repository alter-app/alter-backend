package com.dreamteam.alter.domain.posting.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(
    name = "posting_keyword_map",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_posting_keyword_map",
            columnNames = {"posting_id", "keyword_id"}
        )
    }
)
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostingKeywordMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "posting_id", nullable = false)
    private Posting posting;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "keyword_id", nullable = false)
    private PostingKeyword postingKeyword;

    public static PostingKeywordMap create(PostingKeyword postingKeyword, Posting posting) {
        return PostingKeywordMap.builder()
            .posting(posting)
            .postingKeyword(postingKeyword)
            .build();
    }

}
