package com.dreamteam.alter.adapter.outbound.terms.persistence;

import com.dreamteam.alter.domain.terms.entity.QTerms;
import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.port.outbound.TermsQueryRepository;
import com.dreamteam.alter.domain.terms.type.TermsStatus;
import com.dreamteam.alter.domain.terms.type.TermsType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.Comparator;
import java.util.stream.Collectors;
import jakarta.persistence.LockModeType;
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
    public long countByFilter(TermsType type, TermsStatus status) {
        Long count = queryFactory
                .select(terms.count())
                .from(terms)
                .where(
                        status == null ? notDeleted() : null,
                        typeCondition(type),
                        statusCondition(status)
                )
                .fetchOne();
        return count != null ? count : 0L;
    }

    @Override
    public List<Terms> findByFilter(TermsType type, TermsStatus status, int page, int pageSize) {
        return queryFactory
                .selectFrom(terms)
                .where(
                        status == null ? notDeleted() : null,
                        typeCondition(type),
                        statusCondition(status)
                )
                .orderBy(terms.createdAt.desc(), terms.id.desc())
                .offset((long) (page - 1) * pageSize)
                .limit(pageSize)
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

    @Override
    public Optional<Terms> findPublishedByTypeWithLock(TermsType type) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(terms)
                        .where(
                                terms.type.eq(type),
                                terms.status.eq(TermsStatus.PUBLISHED)
                        )
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                        .fetchOne()
        );
    }

    @Override
    public List<Terms> findLatestPublishedPerType() {
        QTerms sub = new QTerms("sub");

        List<Terms> result = queryFactory
                .selectFrom(terms)
                .where(
                        terms.status.eq(TermsStatus.PUBLISHED),
                        terms.effectiveAt.eq(
                                JPAExpressions
                                        .select(sub.effectiveAt.max())
                                        .from(sub)
                                        .where(
                                                sub.type.eq(terms.type),
                                                sub.status.eq(TermsStatus.PUBLISHED)
                                        )
                        )
                )
                .orderBy(terms.type.asc(), terms.effectiveAt.desc(), terms.id.desc())
                .fetch();

        // 동일 effective_at 중복 방어: 타입별 id 내림차순 기준 첫 번째 1건만 취함
        return result.stream()
                .collect(Collectors.toMap(
                        Terms::getType,
                        t -> t,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(Terms::getType))
                .toList();
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
