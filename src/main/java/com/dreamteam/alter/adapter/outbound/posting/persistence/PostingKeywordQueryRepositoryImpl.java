package com.dreamteam.alter.adapter.outbound.posting.persistence;

import com.dreamteam.alter.domain.posting.entity.PostingKeyword;
import com.dreamteam.alter.domain.posting.entity.QPostingKeyword;
import com.dreamteam.alter.domain.posting.port.outbound.PostingKeywordQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostingKeywordQueryRepositoryImpl implements PostingKeywordQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PostingKeyword> findByIds(List<Long> keywordIds) {
        QPostingKeyword qPostingKeyword = QPostingKeyword.postingKeyword;

        return queryFactory
            .select(qPostingKeyword)
            .from(qPostingKeyword)
            .where(qPostingKeyword.id.in(keywordIds))
            .fetch();
    }

}
