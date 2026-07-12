package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.entity.QBusinessType;
import com.dreamteam.alter.domain.workspace.entity.QWorkspace;
import com.dreamteam.alter.domain.workspace.entity.QWorkspaceRequest;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessTypeQueryRepository;
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
    public boolean existsWorkspaceUsingBusinessType(Long businessTypeId) {
        QWorkspace qWorkspace = QWorkspace.workspace;

        Integer result = queryFactory
            .selectOne()
            .from(qWorkspace)
            .where(qWorkspace.businessType.id.eq(businessTypeId))
            .fetchFirst();

        return result != null;
    }

    @Override
    public boolean existsWorkspaceRequestUsingBusinessType(Long businessTypeId) {
        QWorkspaceRequest qWorkspaceRequest = QWorkspaceRequest.workspaceRequest;

        Integer result = queryFactory
            .selectOne()
            .from(qWorkspaceRequest)
            .where(qWorkspaceRequest.businessType.id.eq(businessTypeId))
            .fetchFirst();

        return result != null;
    }
}
