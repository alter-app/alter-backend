package com.dreamteam.alter.adapter.outbound.notification.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.notification.persistence.readonly.NotificationResponse;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.entity.QNotification;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationQueryRepository;
import com.dreamteam.alter.domain.user.entity.User;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepositoryImpl implements NotificationQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<NotificationResponse> getNotificationsWithCursor(
        CursorPageRequest<CursorDto> pageRequest,
        User targetUser,
        TokenScope scope
    ) {
        QNotification notification = QNotification.notification;

        return queryFactory
            .select(Projections.constructor(
                NotificationResponse.class,
                notification.id,
                notification.title,
                notification.body,
                notification.createdAt
            ))
            .from(notification)
            .where(
                notification.targetUser.eq(targetUser),
                notification.scope.eq(scope),
                cursorCondition(notification, pageRequest.cursor())
            )
            .orderBy(notification.createdAt.desc(), notification.id.desc())
            .limit(pageRequest.pageSize())
            .fetch();
    }

    @Override
    public long getCountOfNotifications(User targetUser, TokenScope scope) {
        QNotification notification = QNotification.notification;

        Long count = queryFactory
            .select(notification.id.count())
            .from(notification)
            .where(
                notification.targetUser.eq(targetUser),
                notification.scope.eq(scope)
            )
            .fetchOne();

        return ObjectUtils.isEmpty(count) ? 0 : count;
    }

    private BooleanExpression cursorCondition(
        QNotification notification,
        CursorDto cursor
    ) {
        if (ObjectUtils.isEmpty(cursor)) {
            return null;
        }

        return notification.createdAt.lt(cursor.getCreatedAt())
            .or(notification.createdAt.eq(cursor.getCreatedAt())
                .and(notification.id.lt(cursor.getId())));
    }
}
