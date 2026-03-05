package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.BusinessJoinRequest;
import com.dreamteam.alter.domain.workspace.entity.QBusinessJoinRequest;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessJoinRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.type.BusinessJoinRequestStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BusinessJoinRequestQueryRepositoryImpl implements BusinessJoinRequestQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<BusinessJoinRequest> findById(Long id) {
        QBusinessJoinRequest qBusinessJoinRequest = QBusinessJoinRequest.businessJoinRequest;

        return Optional.ofNullable(
            queryFactory.selectFrom(qBusinessJoinRequest)
                .where(qBusinessJoinRequest.id.eq(id))
                .fetchOne()
        );
    }

    @Override
    public boolean existsPendingRequest(Workspace workspace, User user) {
        QBusinessJoinRequest qBusinessJoinRequest = QBusinessJoinRequest.businessJoinRequest;

        Long count = queryFactory
            .select(qBusinessJoinRequest.count())
            .from(qBusinessJoinRequest)
            .where(qBusinessJoinRequest.workspace.eq(workspace)
                .and(qBusinessJoinRequest.user.eq(user))
                .and(qBusinessJoinRequest.status.eq(BusinessJoinRequestStatus.PENDING)))
            .fetchOne();

        return count != null && count > 0;
    }

    @Override
    public List<BusinessJoinRequest> findPendingByWorkspace(Workspace workspace) {
        QBusinessJoinRequest qBusinessJoinRequest = QBusinessJoinRequest.businessJoinRequest;

        return queryFactory.selectFrom(qBusinessJoinRequest)
            .join(qBusinessJoinRequest.user).fetchJoin()
            .where(qBusinessJoinRequest.workspace.eq(workspace)
                .and(qBusinessJoinRequest.status.eq(BusinessJoinRequestStatus.PENDING)))
            .fetch();
    }

    @Override
    public List<BusinessJoinRequest> findPendingByUser(User user) {
        QBusinessJoinRequest qBusinessJoinRequest = QBusinessJoinRequest.businessJoinRequest;

        return queryFactory.selectFrom(qBusinessJoinRequest)
            .join(qBusinessJoinRequest.workspace).fetchJoin()
            .where(qBusinessJoinRequest.user.eq(user)
                .and(qBusinessJoinRequest.status.eq(BusinessJoinRequestStatus.PENDING)))
            .fetch();
    }
}
