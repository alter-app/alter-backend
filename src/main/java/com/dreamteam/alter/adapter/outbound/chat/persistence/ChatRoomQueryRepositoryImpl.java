package com.dreamteam.alter.adapter.outbound.chat.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.ChatRoomCursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatRoomListWithOpponentResponse;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.QChatRoom;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.user.entity.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChatRoomQueryRepositoryImpl implements ChatRoomQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final ChatRoomJpaRepository chatRoomJpaRepository;

    @Override
    public Optional<ChatRoom> findById(Long id) {
        return chatRoomJpaRepository.findById(id);
    }

    @Override
    public Optional<ChatRoom> findExistingChatRoom(
        Long participant1Id,
        TokenScope participant1Scope,
        Long participant2Id,
        TokenScope participant2Scope
    ) {
        QChatRoom qChatRoom = QChatRoom.chatRoom;

        BooleanExpression condition = buildChatRoomCondition(
            qChatRoom,
            participant1Id,
            participant1Scope,
            participant2Id,
            participant2Scope
        );

        ChatRoom result = queryFactory
            .selectFrom(qChatRoom)
            .where(condition)
            .orderBy(qChatRoom.createdAt.desc())
            .limit(1)
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<ChatRoomListWithOpponentResponse> getChatRoomListWithOpponent(
        Long userId,
        TokenScope userScope,
        CursorPageRequest<ChatRoomCursorDto> pageRequest
    ) {
        QChatRoom qChatRoom = QChatRoom.chatRoom;

        BooleanExpression participantCondition = buildParticipantCondition(
            qChatRoom,
            userId,
            userScope
        );

        BooleanExpression cursorCondition = buildCursorCondition(
            qChatRoom,
            pageRequest.cursor()
        );

        // 상대방 ID와 scope 결정
        SimpleExpression<Long> opponentIdCase = new CaseBuilder()
            .when(qChatRoom.participant1Id.eq(userId)
                .and(qChatRoom.participant1Scope.eq(userScope)))
            .then(qChatRoom.participant2Id)
            .otherwise(qChatRoom.participant1Id);

        SimpleExpression<TokenScope> opponentScopeCase = new CaseBuilder()
            .when(qChatRoom.participant1Id.eq(userId)
                .and(qChatRoom.participant1Scope.eq(userScope)))
            .then(qChatRoom.participant2Scope)
            .otherwise(qChatRoom.participant1Scope);

        // 상대방 User 조인 (participant1과 participant2 모두 조인)
        QUser qParticipant1User = new QUser("participant1User");
        QUser qParticipant2User = new QUser("participant2User");

        // 상대방 이름 결정 (현재 사용자가 participant1이면 participant2User.name, 그렇지 않으면 participant1User.name)
        SimpleExpression<String> opponentNameCase = new CaseBuilder()
            .when(qChatRoom.participant1Id.eq(userId)
                .and(qChatRoom.participant1Scope.eq(userScope)))
            .then(qParticipant2User.name)
            .otherwise(qParticipant1User.name);

        // 채팅방 정보와 상대방 이름을 함께 조회
        return queryFactory
            .select(Projections.fields(
                ChatRoomListWithOpponentResponse.class,
                qChatRoom.id.as("id"),
                qChatRoom.createdAt.as("createdAt"),
                qChatRoom.updatedAt.as("updatedAt"),
                opponentIdCase.as("opponentId"),
                opponentScopeCase.as("opponentScope"),
                opponentNameCase.as("opponentName")
            ))
            .from(qChatRoom)
            .leftJoin(qParticipant1User)
            .on(qChatRoom.participant1Id.eq(qParticipant1User.id))
            .leftJoin(qParticipant2User)
            .on(qChatRoom.participant2Id.eq(qParticipant2User.id))
            .where(
                participantCondition,
                cursorCondition
            )
            .orderBy(qChatRoom.updatedAt.desc(), qChatRoom.id.desc())
            .limit(pageRequest.pageSize())
            .fetch();
    }

    @Override
    public Optional<ChatRoom> findByIdAndParticipant(
        Long id,
        Long userId,
        TokenScope userScope
    ) {
        QChatRoom qChatRoom = QChatRoom.chatRoom;

        BooleanExpression idCondition = qChatRoom.id.eq(id);
        BooleanExpression participantCondition = buildParticipantCondition(
            qChatRoom,
            userId,
            userScope
        );

        ChatRoom result = queryFactory
            .selectFrom(qChatRoom)
            .where(
                idCondition,
                participantCondition
            )
            .fetchOne();

        return Optional.ofNullable(result);
    }

    private BooleanExpression buildChatRoomCondition(
        QChatRoom qChatRoom,
        Long participant1Id,
        TokenScope participant1Scope,
        Long participant2Id,
        TokenScope participant2Scope
    ) {
        BooleanExpression case1 = qChatRoom.participant1Id.eq(participant1Id)
            .and(qChatRoom.participant1Scope.eq(participant1Scope))
            .and(qChatRoom.participant2Id.eq(participant2Id))
            .and(qChatRoom.participant2Scope.eq(participant2Scope));

        BooleanExpression case2 = qChatRoom.participant1Id.eq(participant2Id)
            .and(qChatRoom.participant1Scope.eq(participant2Scope))
            .and(qChatRoom.participant2Id.eq(participant1Id))
            .and(qChatRoom.participant2Scope.eq(participant1Scope));

        return case1.or(case2);
    }

    private BooleanExpression buildParticipantCondition(
        QChatRoom qChatRoom,
        Long userId,
        TokenScope userScope
    ) {
        return (qChatRoom.participant1Id.eq(userId)
            .and(qChatRoom.participant1Scope.eq(userScope)))
            .or(qChatRoom.participant2Id.eq(userId)
                .and(qChatRoom.participant2Scope.eq(userScope)));
    }

    private BooleanExpression buildCursorCondition(
        QChatRoom qChatRoom,
        ChatRoomCursorDto cursor
    ) {
        if (ObjectUtils.isEmpty(cursor)) {
            return null;
        }

        return qChatRoom.updatedAt.lt(cursor.getUpdatedAt())
            .or(
                qChatRoom.updatedAt.eq(cursor.getUpdatedAt())
                    .and(qChatRoom.id.lt(cursor.getId()))
            );
    }
}
