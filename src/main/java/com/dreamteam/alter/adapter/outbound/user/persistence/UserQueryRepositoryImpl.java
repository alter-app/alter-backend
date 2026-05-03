package com.dreamteam.alter.adapter.outbound.user.persistence;

import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.UserSelfInfoResponse;
import com.dreamteam.alter.domain.file.entity.QFile;
import com.dreamteam.alter.domain.file.type.FileStatus;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.reputation.entity.QReputationSummary;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.user.entity.QUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.type.UserStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        QReputationSummary qReputationSummary = QReputationSummary.reputationSummary;
        QFile qFile = QFile.file;

        UserSelfInfoResponse userSelf = queryFactory.select(
                Projections.constructor(
                    UserSelfInfoResponse.class,
                    qUser.id,
                    qUser.name,
                    qUser.nickname,
                    qUser.createdAt,
                    qFile.fileUrl,
                    qReputationSummary
                )
            )
            .from(qUser)
            .leftJoin(qReputationSummary).on(
                qReputationSummary.targetType.eq(ReputationType.USER),
                qReputationSummary.targetId.eq(qUser.id)
            )
            .leftJoin(qFile).on(
                qFile.targetType.eq(FileTargetType.USER_PROFILE),
                qFile.targetId.eq(qUser.id.stringValue()),
                qFile.status.eq(FileStatus.ATTACHED)
            )
            .where(
                qUser.id.eq(id),
                qUser.status.eq(UserStatus.ACTIVE)
            )
            .fetchOne();

        return Optional.ofNullable(userSelf);
    }

    @Override
    public List<User> findAllById(List<Long> ids) {
        QUser qUser = QUser.user;
        return queryFactory.selectFrom(qUser)
            .where(
                qUser.id.in(ids),
                qUser.status.eq(UserStatus.ACTIVE)
            )
            .fetch();
    }

    @Override
    public List<User> findByContactIn(Set<String> contacts) {
        QUser qUser = QUser.user;
        return queryFactory.selectFrom(qUser)
            .where(
                qUser.contact.in(contacts),
                qUser.status.eq(UserStatus.ACTIVE)
            )
            .fetch();
    }
}
