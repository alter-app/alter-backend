package com.dreamteam.alter.adapter.outbound.posting.persistence;

import com.dreamteam.alter.domain.posting.entity.Keyword;
import com.dreamteam.alter.domain.posting.entity.QKeyword;
import com.dreamteam.alter.domain.posting.port.outbound.KeywordQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class KeywordQueryRepositoryImpl implements KeywordQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Keyword> findByIds(List<Long> keywordIds) {
        QKeyword qKeyword = QKeyword.keyword;

        return queryFactory
            .select(qKeyword)
            .from(qKeyword)
            .where(qKeyword.id.in(keywordIds))
            .fetch();
    }

}
