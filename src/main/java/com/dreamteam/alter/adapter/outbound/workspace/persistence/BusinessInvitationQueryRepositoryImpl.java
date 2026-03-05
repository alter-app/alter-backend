package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;
import com.dreamteam.alter.domain.workspace.entity.QBusinessInvitation;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessInvitationQueryRepository;
import com.dreamteam.alter.domain.workspace.type.BusinessInvitationStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BusinessInvitationQueryRepositoryImpl implements BusinessInvitationQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<BusinessInvitation> findById(Long id) {
        QBusinessInvitation qBusinessInvitation = QBusinessInvitation.businessInvitation;

        return Optional.ofNullable(
            queryFactory.selectFrom(qBusinessInvitation)
                .where(qBusinessInvitation.id.eq(id))
                .fetchOne()
        );
    }

    @Override
    public boolean existsPendingInvitation(Workspace workspace, User user) {
        QBusinessInvitation qBusinessInvitation = QBusinessInvitation.businessInvitation;

        Long count = queryFactory
            .select(qBusinessInvitation.count())
            .from(qBusinessInvitation)
            .where(qBusinessInvitation.workspace.eq(workspace)
                .and(qBusinessInvitation.invitedUser.eq(user))
                .and(qBusinessInvitation.status.eq(BusinessInvitationStatus.PENDING)))
            .fetchOne();

        return count != null && count > 0;
    }

    @Override
    public List<BusinessInvitation> findPendingByUser(User user) {
        QBusinessInvitation qBusinessInvitation = QBusinessInvitation.businessInvitation;

        return queryFactory.selectFrom(qBusinessInvitation)
            .join(qBusinessInvitation.workspace).fetchJoin()
            .where(qBusinessInvitation.invitedUser.eq(user)
                .and(qBusinessInvitation.status.eq(BusinessInvitationStatus.PENDING)))
            .fetch();
    }
}
