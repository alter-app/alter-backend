package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.entity.QBusinessType;
import com.dreamteam.alter.domain.workspace.entity.QWorkspace;
import com.dreamteam.alter.domain.workspace.entity.QWorkspaceRequest;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessTypeQueryRepository;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BusinessTypeQueryRepositoryImpl implements BusinessTypeQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<BusinessType> findAll() {
        QBusinessType qBusinessType = QBusinessType.businessType;

        // '기타'(requires_detail=true)를 목록 맨 뒤로 정렬
        return queryFactory
            .selectFrom(qBusinessType)
            .orderBy(qBusinessType.requiresDetail.asc(), qBusinessType.id.asc())
            .fetch();
    }

    @Override
    public boolean existsReferenced(Long businessTypeId) {
        QBusinessType qBusinessType = QBusinessType.businessType;
        QWorkspace qWorkspace = QWorkspace.workspace;
        QWorkspaceRequest qWorkspaceRequest = QWorkspaceRequest.workspaceRequest;

        // 업장/업장 신청 두 테이블을 EXISTS 서브쿼리로 묶어 단일 쿼리로 참조 여부를 확인한다.
        Integer result = queryFactory
            .selectOne()
            .from(qBusinessType)
            .where(
                qBusinessType.id.eq(businessTypeId),
                JPAExpressions.selectOne()
                    .from(qWorkspace)
                    .where(qWorkspace.businessType.id.eq(businessTypeId))
                    .exists()
                    .or(JPAExpressions.selectOne()
                        .from(qWorkspaceRequest)
                        .where(qWorkspaceRequest.businessType.id.eq(businessTypeId))
                        .exists())
            )
            .fetchFirst();

        return result != null;
    }
}
