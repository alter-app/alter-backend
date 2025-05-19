package com.dreamteam.alter.domain.posting.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "posting_keyword_map")
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
    private Keyword keyword;

    public static PostingKeywordMap create(Keyword keyword, Posting posting) {
        return PostingKeywordMap.builder()
            .posting(posting)
            .keyword(keyword)
            .build();
    }

}
