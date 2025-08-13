package com.dreamteam.alter.adapter.outbound.reputation.persistence;

import com.dreamteam.alter.domain.reputation.entity.QReputationKeyword;
import com.dreamteam.alter.domain.reputation.entity.ReputationKeyword;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationKeywordQueryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationKeywordStatus;
import com.dreamteam.alter.domain.reputation.type.ReputationKeywordType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ReputationKeywordQueryRepositoryImpl implements ReputationKeywordQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ReputationKeyword> findAllByKeywordType(ReputationKeywordType keywordType) {
        QReputationKeyword qReputationKeyword = QReputationKeyword.reputationKeyword;

        return queryFactory.selectFrom(qReputationKeyword)
            .where(
                qReputationKeyword.type.eq(keywordType),
                qReputationKeyword.status.eq(ReputationKeywordStatus.ACTIVATED)
            )
            .fetch();
    }

    @Override
    public List<ReputationKeyword> findByIdList(Set<String> keywordIds) {
        QReputationKeyword qReputationKeyword = QReputationKeyword.reputationKeyword;

        return queryFactory.selectFrom(qReputationKeyword)
            .where(
                qReputationKeyword.id.in(keywordIds),
                qReputationKeyword.status.eq(ReputationKeywordStatus.ACTIVATED)
            )
            .fetch();
    }
}
