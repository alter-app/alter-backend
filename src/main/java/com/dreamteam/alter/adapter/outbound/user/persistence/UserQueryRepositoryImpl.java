package com.dreamteam.alter.adapter.outbound.user.persistence;

import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.UserSelfInfoResponse;
import com.dreamteam.alter.domain.user.entity.QUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.type.UserStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<User> findById(Long id) {
        QUser qUser = QUser.user;
        User user = queryFactory.select(qUser)
            .from(qUser)
            .where(
                qUser.id.eq(id),
                qUser.status.eq(UserStatus.ACTIVE)
            )
            .fetchOne();

        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        QUser user = QUser.user;
        User foundUser = queryFactory
            .selectFrom(user)
            .where(
                user.email.eq(email),
                user.status.eq(UserStatus.ACTIVE)
            )
            .fetchOne();

        return Optional.ofNullable(foundUser);
    }

    @Override
    public Optional<User> findByNickname(String nickname) {
        QUser user = QUser.user;
        User foundUser = queryFactory
            .selectFrom(user)
            .where(
                user.nickname.eq(nickname),
                user.status.eq(UserStatus.ACTIVE)
            )
            .fetchOne();

        return Optional.ofNullable(foundUser);
    }

    @Override
    public Optional<User> findByContact(String contact) {
        QUser qUser = QUser.user;
        User foundUser = queryFactory
            .selectFrom(qUser)
            .where(
                qUser.contact.eq(contact),
                qUser.status.eq(UserStatus.ACTIVE)
            )
            .fetchOne();

        return Optional.ofNullable(foundUser);
    }

    @Override
    public Optional<UserSelfInfoResponse> getUserSelfInfoSummary(Long id) {
        QUser qUser = QUser.user;

        UserSelfInfoResponse userSelf = queryFactory.select(
                Projections.fields(
                    UserSelfInfoResponse.class,
                    qUser.id,
                    qUser.name,
                    qUser.nickname,
                    qUser.createdAt
                )
            )
            .from(qUser)
            .where(
                qUser.id.eq(id),
                qUser.status.eq(UserStatus.ACTIVE)
            )
            .fetchOne();

        return Optional.ofNullable(userSelf);
    }
}
