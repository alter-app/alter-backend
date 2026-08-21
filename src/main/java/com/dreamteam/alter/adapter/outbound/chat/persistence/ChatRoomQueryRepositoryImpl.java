package com.dreamteam.alter.adapter.outbound.chat.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.ChatRoomCursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatRoomListWithOpponentResponse;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.QChatRoom;
import com.dreamteam.alter.domain.chat.entity.QChatRoomMember;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import com.dreamteam.alter.domain.file.entity.QFile;
import com.dreamteam.alter.domain.file.type.FileStatus;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.QUser;
import com.dreamteam.alter.domain.user.type.UserStatus;
import com.dreamteam.alter.domain.workspace.entity.QWorkspace;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.jpa.JPAExpressions;
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
        NumberExpression<Long> opponentIdCase = new CaseBuilder()
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

        // 상대방(활성 상태만) User id: 비활성 상대는 join 조건(ACTIVE)에 걸려 null이 된다
        NumberExpression<Long> opponentActiveUserIdCase = new CaseBuilder()
            .when(qChatRoom.participant1Id.eq(userId)
                .and(qChatRoom.participant1Scope.eq(userScope)))
            .then(qParticipant2User.id)
            .otherwise(qParticipant1User.id);

        // 상대방 프로필 이미지 (가장 오래된 ATTACHED 파일 1건) 스칼라 서브쿼리
        // - leftJoin 방식은 동일 대상에 ATTACHED 파일이 2건 이상이면 방 행이 복제되므로 사용하지 않는다
        // - 비활성 상대는 opponentActiveUserIdCase가 null이 되어 url도 null로 유지된다
        // - JPQL 서브쿼리는 LIMIT을 지원하지 않으므로 "더 오래된 행이 없다(NOT EXISTS)"로 1건만 선택한다
        QFile qFile = QFile.file;
        QFile qOlderFile = new QFile("olderFile");
        Expression<String> opponentProfileImageUrlExpr = ExpressionUtils.as(
            JPAExpressions
                .select(qFile.fileUrl)
                .from(qFile)
                .where(
                    qFile.targetType.eq(FileTargetType.USER_PROFILE),
                    qFile.status.eq(FileStatus.ATTACHED),
                    qFile.targetId.eq(opponentActiveUserIdCase.stringValue()),
                    JPAExpressions
                        .selectOne()
                        .from(qOlderFile)
                        .where(
                            qOlderFile.targetType.eq(FileTargetType.USER_PROFILE),
                            qOlderFile.status.eq(FileStatus.ATTACHED),
                            qOlderFile.targetId.eq(qFile.targetId),
                            qOlderFile.createdAt.lt(qFile.createdAt)
                                .or(qOlderFile.createdAt.eq(qFile.createdAt)
                                    .and(qOlderFile.id.lt(qFile.id)))
                        )
                        .notExists()
                ),
            "opponentProfileImageUrl"
        );

        // 그룹 방은 상대방이 없으므로 업장명을 방 이름으로 사용한다
        QWorkspace qWorkspace = QWorkspace.workspace;

        // 참여 인원 수(나간 멤버 제외) 스칼라 서브쿼리
        QChatRoomMember qMemberCount = QChatRoomMember.chatRoomMember;
        Expression<Long> memberCountExpr = ExpressionUtils.as(
            JPAExpressions
                .select(qMemberCount.count())
                .from(qMemberCount)
                .where(
                    qMemberCount.chatRoomId.eq(qChatRoom.id),
                    qMemberCount.leftAt.isNull()
                ),
            "memberCount"
        );

        // 채팅방 정보와 상대방 이름을 함께 조회
        return queryFactory
            .select(Projections.fields(
                ChatRoomListWithOpponentResponse.class,
                qChatRoom.id.as("id"),
                qChatRoom.type.as("type"),
                qChatRoom.createdAt.as("createdAt"),
                qChatRoom.updatedAt.as("updatedAt"),
                qWorkspace.businessName.as("workspaceName"),
                opponentIdCase.as("opponentId"),
                opponentScopeCase.as("opponentScope"),
                opponentNameCase.as("opponentName"),
                opponentProfileImageUrlExpr,
                memberCountExpr
            ))
            .from(qChatRoom)
            .leftJoin(qParticipant1User)
            .on(qChatRoom.participant1Id.eq(qParticipant1User.id)
                .and(qParticipant1User.status.eq(UserStatus.ACTIVE)))
            .leftJoin(qParticipant2User)
            .on(qChatRoom.participant2Id.eq(qParticipant2User.id)
                .and(qParticipant2User.status.eq(UserStatus.ACTIVE)))
            .leftJoin(qWorkspace)
            .on(qChatRoom.workspaceId.eq(qWorkspace.id))
            .where(
                participantCondition,
                cursorCondition
            )
            .orderBy(qChatRoom.updatedAt.desc(), qChatRoom.id.desc())
            .limit(pageRequest.pageSize())
            .fetch();
    }

    @Override
    public long countChatRoomsByParticipant(Long userId, TokenScope userScope) {
        QChatRoom qChatRoom = QChatRoom.chatRoom;

        Long count = queryFactory
            .select(qChatRoom.count())
            .from(qChatRoom)
            .where(buildParticipantCondition(qChatRoom, userId, userScope))
            .fetchOne();

        return count != null ? count : 0L;
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

    @Override
    public Optional<ChatRoom> findGroupRoomByWorkspaceId(Long workspaceId) {
        QChatRoom qChatRoom = QChatRoom.chatRoom;

        ChatRoom result = queryFactory
            .selectFrom(qChatRoom)
            .where(
                qChatRoom.type.eq(ChatRoomType.GROUP),
                qChatRoom.workspaceId.eq(workspaceId)
            )
            // 유니크 인덱스(V7)로 단일성이 보장되지만, 혹시 모를 중복 데이터에도
            // NonUniqueResultException을 던지지 않도록 fetchFirst 사용
            .fetchFirst();

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

    // 참여 판정은 DIRECT/GROUP 공통으로 chat_room_members(활성 멤버) 기준이다.
    // V11 마이그레이션이 기존 방을 멤버 2행으로 백필했고 신규 DIRECT 방도 멤버 행을 만들므로
    // participant 컬럼을 함께 보지 않아도 누락이 없다. (OR 를 없애야 멤버 인덱스를 탄다)
    private BooleanExpression buildParticipantCondition(
        QChatRoom qChatRoom,
        Long userId,
        TokenScope userScope
    ) {
        QChatRoomMember qChatRoomMember = QChatRoomMember.chatRoomMember;

        return JPAExpressions
            .selectOne()
            .from(qChatRoomMember)
            .where(
                qChatRoomMember.chatRoomId.eq(qChatRoom.id),
                qChatRoomMember.memberId.eq(userId),
                qChatRoomMember.memberScope.eq(userScope),
                qChatRoomMember.leftAt.isNull()
            )
            .exists();
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
