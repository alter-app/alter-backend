package com.dreamteam.alter.adapter.outbound.user.persistence;

import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUserListFilterDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.AdminUserDetailResponse;
import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.AdminUserListResponse;
import com.dreamteam.alter.domain.reputation.entity.QReputationSummary;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.user.entity.QUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.AdminUserQueryRepository;
import com.dreamteam.alter.domain.user.type.UserRole;
import com.dreamteam.alter.domain.user.type.UserStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AdminUserQueryRepositoryImpl implements AdminUserQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QUser user = QUser.user;
    private final QReputationSummary reputationSummary = QReputationSummary.reputationSummary;

    @Override
    public long getUserCount(AdminUserListFilterDto filter) {
        Long count = queryFactory
            .select(user.count())
            .from(user)
            .where(
                user.status.ne(UserStatus.DELETED),
                eqStatus(filter.getStatus()),
                eqRole(filter.getRole()),
                containsEmail(filter.getEmail()),
                containsName(filter.getName()),
                containsNickname(filter.getNickname()),
                containsContact(filter.getContact())
            )
            .fetchOne();

        return count != null ? count : 0L;
    }

    @Override
    public List<AdminUserListResponse> getUserListUsingPagination(
        PageRequestDto pageRequest,
        AdminUserListFilterDto filter
    ) {
        return queryFactory
            .select(Projections.constructor(
                AdminUserListResponse.class,
                user.id,
                user.email,
                user.name,
                user.nickname,
                user.role,
                user.status,
                user.createdAt
            ))
            .from(user)
            .where(
                user.status.ne(UserStatus.DELETED),
                eqStatus(filter.getStatus()),
                eqRole(filter.getRole()),
                containsEmail(filter.getEmail()),
                containsName(filter.getName()),
                containsNickname(filter.getNickname()),
                containsContact(filter.getContact())
            )
            .orderBy(user.createdAt.desc(), user.id.desc())
            .offset(pageRequest.getOffset())
            .limit(pageRequest.getLimit())
            .fetch();
    }

    @Override
    public Optional<AdminUserDetailResponse> getUserDetail(Long userId) {
        AdminUserDetailResponse response = queryFactory
            .select(Projections.constructor(
                AdminUserDetailResponse.class,
                user.id,
                user.email,
                user.name,
                user.nickname,
                user.contact,
                user.birthday,
                user.gender,
                user.role,
                user.status,
                user.createdAt,
                user.updatedAt,
                reputationSummary
            ))
            .from(user)
            .leftJoin(reputationSummary)
            .on(
                reputationSummary.targetType.eq(ReputationType.USER),
                reputationSummary.targetId.eq(user.id)
            )
            .where(
                user.id.eq(userId),
                user.status.ne(UserStatus.DELETED)
            )
            .fetchOne();

        return Optional.ofNullable(response);
    }

    @Override
    public Optional<User> findById(Long userId) {
        User foundUser = queryFactory
            .selectFrom(user)
            .where(
                user.id.eq(userId),
                user.status.ne(UserStatus.DELETED)
            )
            .fetchOne();

        return Optional.ofNullable(foundUser);
    }

    private BooleanExpression eqStatus(UserStatus status) {
        return ObjectUtils.isNotEmpty(status) ? user.status.eq(status) : null;
    }

    private BooleanExpression eqRole(UserRole role) {
        return ObjectUtils.isNotEmpty(role) ? user.role.eq(role) : null;
    }

    private BooleanExpression containsEmail(String email) {
        return ObjectUtils.isNotEmpty(email)
            ? user.email.containsIgnoreCase(email) : null;
    }

    private BooleanExpression containsName(String name) {
        return ObjectUtils.isNotEmpty(name)
            ? user.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression containsNickname(String nickname) {
        return ObjectUtils.isNotEmpty(nickname)
            ? user.nickname.containsIgnoreCase(nickname) : null;
    }

    private BooleanExpression containsContact(String contact) {
        return ObjectUtils.isNotEmpty(contact)
            ? user.contact.containsIgnoreCase(contact) : null;
    }
}
