package com.dreamteam.alter.adapter.outbound.user.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.QManagerUser;
import com.dreamteam.alter.domain.user.port.outbound.ManagerUserQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ManagerUserQueryRepositoryImpl implements ManagerUserQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ManagerUser> findByUserId(Long userId) {
        QManagerUser qManagerUser = QManagerUser.managerUser;

        return Optional.ofNullable(queryFactory
            .selectFrom(qManagerUser)
            .where(qManagerUser.user.id.eq(userId))
            .fetchOne());
    }

    @Override
    public Optional<ManagerUser> findById(Long id) {
        QManagerUser qManagerUser = QManagerUser.managerUser;

        return Optional.ofNullable(queryFactory
            .selectFrom(qManagerUser)
            .where(qManagerUser.id.eq(id))
            .fetchOne());
    }

}
