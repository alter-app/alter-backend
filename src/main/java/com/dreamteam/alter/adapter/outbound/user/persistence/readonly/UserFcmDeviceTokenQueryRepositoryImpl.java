package com.dreamteam.alter.adapter.outbound.user.persistence.readonly;

import com.dreamteam.alter.domain.user.entity.FcmDeviceToken;
import com.dreamteam.alter.domain.user.entity.QFcmDeviceToken;
import com.dreamteam.alter.domain.user.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserFcmDeviceTokenQueryRepositoryImpl implements UserFcmDeviceTokenQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<FcmDeviceToken> findByUser(User user) {
        QFcmDeviceToken qFcmDeviceToken = QFcmDeviceToken.fcmDeviceToken;

        return Optional.ofNullable(queryFactory
            .selectFrom(qFcmDeviceToken)
            .where(qFcmDeviceToken.user.eq(user))
            .fetchOne());
    }

    @Override
    public Optional<FcmDeviceToken> findByDeviceToken(String deviceToken) {
        QFcmDeviceToken qFcmDeviceToken = QFcmDeviceToken.fcmDeviceToken;

        return Optional.ofNullable(queryFactory
            .selectFrom(qFcmDeviceToken)
            .where(qFcmDeviceToken.deviceToken.eq(deviceToken))
            .fetchOne());
    }

}
