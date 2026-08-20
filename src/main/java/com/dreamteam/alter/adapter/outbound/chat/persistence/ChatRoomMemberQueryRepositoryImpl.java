package com.dreamteam.alter.adapter.outbound.chat.persistence;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.entity.QChatRoomMember;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ChatRoomMemberQueryRepositoryImpl implements ChatRoomMemberQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatRoomMember> findActiveByRoom(Long chatRoomId) {
        QChatRoomMember m = QChatRoomMember.chatRoomMember;
        return queryFactory.selectFrom(m)
            .where(m.chatRoomId.eq(chatRoomId), m.leftAt.isNull())
            .fetch();
    }

    @Override
    public Optional<ChatRoomMember> findByRoomAndMember(Long chatRoomId, Long memberId, TokenScope memberScope) {
        QChatRoomMember m = QChatRoomMember.chatRoomMember;
        return Optional.ofNullable(
            queryFactory.selectFrom(m)
                .where(m.chatRoomId.eq(chatRoomId), m.memberId.eq(memberId), m.memberScope.eq(memberScope))
                .fetchOne()
        );
    }

    @Override
    public int countActiveByRoom(Long chatRoomId) {
        return countActiveByRoomIds(List.of(chatRoomId)).getOrDefault(chatRoomId, 0L).intValue();
    }

    @Override
    public Map<Long, Long> countActiveByRoomIds(List<Long> chatRoomIds) {
        if (ObjectUtils.isEmpty(chatRoomIds)) {
            return Map.of();
        }

        QChatRoomMember m = QChatRoomMember.chatRoomMember;
        List<Tuple> counts = queryFactory.select(m.chatRoomId, m.count())
            .from(m)
            .where(m.chatRoomId.in(chatRoomIds), m.leftAt.isNull())
            .groupBy(m.chatRoomId)
            .fetch();

        return counts.stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(m.chatRoomId),
                tuple -> {
                    Long count = tuple.get(m.count());
                    return count != null ? count : 0L;
                }
            ));
    }

    @Override
    public boolean existsActive(Long chatRoomId, Long memberId, TokenScope memberScope) {
        QChatRoomMember m = QChatRoomMember.chatRoomMember;
        Integer hit = queryFactory.selectOne().from(m)
            .where(m.chatRoomId.eq(chatRoomId), m.memberId.eq(memberId),
                m.memberScope.eq(memberScope), m.leftAt.isNull())
            .fetchFirst();
        return hit != null;
    }
}
