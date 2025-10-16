package com.dreamteam.alter.adapter.outbound.user.persistence.readonly;

import com.dreamteam.alter.domain.user.entity.FCMDeviceToken;
import com.dreamteam.alter.domain.user.entity.QFCMDeviceToken;
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
    public Optional<FCMDeviceToken> findByUser(User user) {
        QFCMDeviceToken qfcmDeviceToken = QFCMDeviceToken.fCMDeviceToken;

        return Optional.ofNullable(queryFactory
            .selectFrom(qfcmDeviceToken)
            .where(qfcmDeviceToken.user.eq(user))
            .fetchOne());
    }

    @Override
    public Optional<FCMDeviceToken> findByDeviceToken(String deviceToken) {
        QFCMDeviceToken qfcmDeviceToken = QFCMDeviceToken.fCMDeviceToken;

        return Optional.ofNullable(queryFactory
            .selectFrom(qfcmDeviceToken)
            .where(qfcmDeviceToken.deviceToken.eq(deviceToken))
            .fetchOne());
    }

}
