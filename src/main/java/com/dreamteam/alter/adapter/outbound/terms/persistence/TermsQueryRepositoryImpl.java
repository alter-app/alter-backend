package com.dreamteam.alter.adapter.outbound.terms.persistence;

import com.dreamteam.alter.adapter.inbound.admin.terms.dto.TermsListFilterDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.domain.terms.entity.QTerms;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.outbound.TermsQueryRepository;
import com.dreamteam.alter.domain.terms.type.TermsStatus;
import com.dreamteam.alter.domain.terms.type.TermsType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TermsQueryRepositoryImpl implements TermsQueryRepository {

    private final JPAQueryFactory queryFactory;

    private final QTerms terms = QTerms.terms;

    @Override
    public Optional<Terms> findById(Long id) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(terms)
                        .where(
                                terms.id.eq(id),
                                notDeleted()
                        )
                        .fetchOne()
        );
    }

    @Override
    public long countByFilter(TermsListFilterDto filter) {
        Long count = queryFactory
                .select(terms.count())
                .from(terms)
                .where(
                        filter.getStatus() == null ? notDeleted() : null,
                        typeCondition(filter.getType()),
                        statusCondition(filter.getStatus())
                )
                .fetchOne();
        return count != null ? count : 0L;
    }

    @Override
    public List<Terms> findByFilter(TermsListFilterDto filter, PageRequestDto pageRequest) {
        return queryFactory
                .selectFrom(terms)
                .where(
                        filter.getStatus() == null ? notDeleted() : null,
                        typeCondition(filter.getType()),
                        statusCondition(filter.getStatus())
                )
                .orderBy(terms.createdAt.desc(), terms.id.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getLimit())
                .fetch();
    }

    @Override
    public Optional<Terms> findPublishedByType(TermsType type) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(terms)
                        .where(
                                terms.type.eq(type),
                                terms.status.eq(TermsStatus.PUBLISHED)
                        )
                        .fetchFirst()
        );
    }

    private BooleanExpression notDeleted() {
        return terms.status.ne(TermsStatus.DELETED);
    }

    private BooleanExpression typeCondition(TermsType type) {
        return type != null ? terms.type.eq(type) : null;
    }

    private BooleanExpression statusCondition(TermsStatus status) {
        return status != null ? terms.status.eq(status) : null;
    }
}
