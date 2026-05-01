package com.dreamteam.alter.adapter.outbound.user.persistence;

import com.dreamteam.alter.domain.user.entity.QUser;
import com.dreamteam.alter.domain.user.entity.QUserSocial;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.entity.UserSocial;
import com.dreamteam.alter.domain.user.port.outbound.UserSocialQueryRepository;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import com.dreamteam.alter.domain.user.type.UserStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserSocialQueryRepositoryImpl implements UserSocialQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<UserSocial> findByUserIdAndSocialProvider(Long userId, SocialProvider socialProvider) {
        QUserSocial qUserSocial = QUserSocial.userSocial;

        UserSocial userSocial = queryFactory.selectFrom(qUserSocial)
            .where(
                qUserSocial.user.id.eq(userId),
                qUserSocial.socialProvider.eq(socialProvider)
            )
            .fetchFirst();

        return Optional.ofNullable(userSocial);
    }

    @Override
    public Optional<UserSocial> findBySocialProviderAndSocialId(SocialProvider socialProvider, String socialId) {
        QUserSocial qUserSocial = QUserSocial.userSocial;
        QUser qUser = QUser.user;
        
        UserSocial userSocial = queryFactory.selectFrom(qUserSocial)
            .join(qUserSocial.user, qUser)
            .where(
                qUserSocial.socialProvider.eq(socialProvider),
                qUserSocial.socialId.eq(socialId),
                qUser.status.eq(UserStatus.ACTIVE)
            )
            .fetchOne();
            
        return Optional.ofNullable(userSocial);
    }

    @Override
    public Map<SocialProvider, LocalDateTime> findLinkedSocialAccountsByUserId(User user) {
        QUserSocial qUserSocial = QUserSocial.userSocial;

        return queryFactory.select(qUserSocial.socialProvider, qUserSocial.createdAt)
            .from(qUserSocial)
            .where(qUserSocial.user.eq(user))
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(qUserSocial.socialProvider),
                tuple -> tuple.get(qUserSocial.createdAt)
            ));
    }

    @Override
    public boolean existsBySocialProviderAndSocialId(SocialProvider socialProvider, String socialId) {
        QUserSocial qUserSocial = QUserSocial.userSocial;
        
        Long count = queryFactory.select(qUserSocial.count())
            .from(qUserSocial)
            .where(
                qUserSocial.socialProvider.eq(socialProvider),
                qUserSocial.socialId.eq(socialId),
                qUserSocial.user.status.eq(UserStatus.ACTIVE)
            )
            .fetchOne();
            
        return count != null && count > 0;
    }

    @Override
    public boolean existsByUserAndSocialProvider(Long userId, SocialProvider socialProvider) {
        QUserSocial qUserSocial = QUserSocial.userSocial;

        Long count = queryFactory.select(qUserSocial.count())
            .from(qUserSocial)
            .where(
                qUserSocial.user.id.eq(userId),
                qUserSocial.user.status.eq(UserStatus.ACTIVE),
                qUserSocial.socialProvider.eq(socialProvider)
            )
            .fetchOne();

        return count != null && count > 0;
    }

    @Override
    public long countByUserIdForUpdate(Long userId) {
        QUserSocial q = QUserSocial.userSocial;
        List<Long> ids = queryFactory.select(q.id)
            .from(q)
            .where(q.user.id.eq(userId))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .fetch();
        return ids.size();
    }
}
