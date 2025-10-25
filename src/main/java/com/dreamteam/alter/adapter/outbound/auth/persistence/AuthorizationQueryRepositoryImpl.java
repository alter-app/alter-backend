package com.dreamteam.alter.adapter.outbound.auth.persistence;

import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.auth.entity.QAuthorization;
import com.dreamteam.alter.domain.auth.port.outbound.AuthorizationQueryRepository;
import com.dreamteam.alter.domain.auth.type.AuthorizationStatus;
import com.dreamteam.alter.domain.user.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AuthorizationQueryRepositoryImpl implements AuthorizationQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Authorization> findAllByUser(User user) {
        QAuthorization qAuthorization = QAuthorization.authorization;

        return queryFactory
            .selectFrom(qAuthorization)
            .where(
                qAuthorization.user.eq(user),
                qAuthorization.status.eq(AuthorizationStatus.ACTIVE)
            )
            .fetch();
    }
}
