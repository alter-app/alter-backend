package com.dreamteam.alter.adapter.outbound.user.persistence;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialUserInfo;
import com.dreamteam.alter.domain.user.entity.QUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.type.UserStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public User findBySocialId(String socialId) {
        QUser user = QUser.user;
        return queryFactory
            .selectFrom(user)
            .where(
                user.socialId.eq(socialId),
                user.status.eq(UserStatus.ACTIVE)
            )
            .fetchOne();
    }

    @Override
    public User findByEmail(String email) {
        QUser user = QUser.user;
        return queryFactory
            .selectFrom(user)
            .where(
                user.email.eq(email),
                user.status.eq(UserStatus.ACTIVE)
            )
            .fetchOne();
    }

}
