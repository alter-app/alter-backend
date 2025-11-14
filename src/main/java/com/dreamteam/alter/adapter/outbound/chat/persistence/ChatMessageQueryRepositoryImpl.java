package com.dreamteam.alter.adapter.outbound.chat.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;
import com.dreamteam.alter.domain.chat.entity.QChatMessage;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageQueryRepository;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ChatMessageQueryRepositoryImpl implements ChatMessageQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatMessageResponse> getChatMessagesWithCursor(
        Long chatRoomId,
        CursorPageRequest<CursorDto> pageRequest
    ) {
        QChatMessage qChatMessage = QChatMessage.chatMessage;

        BooleanExpression cursorCondition = buildCursorCondition(
            qChatMessage,
            pageRequest.cursor()
        );

        return queryFactory
            .select(Projections.constructor(
                ChatMessageResponse.class,
                qChatMessage.id,
                qChatMessage.chatRoomId,
                qChatMessage.senderId,
                qChatMessage.senderScope,
                qChatMessage.content,
                qChatMessage.createdAt
            ))
            .from(qChatMessage)
            .where(
                qChatMessage.chatRoomId.eq(chatRoomId),
                cursorCondition
            )
            .orderBy(qChatMessage.createdAt.desc(), qChatMessage.id.desc())
            .limit(pageRequest.pageSize())
            .fetch();
    }

    @Override
    public Map<Long, String> getLatestMessageContentsByChatRoomIds(List<Long> chatRoomIds) {
        if (ObjectUtils.isEmpty(chatRoomIds)) {
            return Map.of();
        }

        QChatMessage qChatMessage = QChatMessage.chatMessage;
        QChatMessage qSubMessage = new QChatMessage("subMessage");

        // 각 채팅방별 최신 메시지 ID 조회
        List<Long> latestMessageIds = queryFactory
            .select(qChatMessage.id)
            .from(qChatMessage)
            .where(
                qChatMessage.chatRoomId.in(chatRoomIds),
                qChatMessage.id.eq(
                    JPAExpressions
                        .select(qSubMessage.id.max())
                        .from(qSubMessage)
                        .where(
                            qSubMessage.chatRoomId.eq(qChatMessage.chatRoomId),
                            qSubMessage.createdAt.eq(
                                JPAExpressions
                                    .select(qSubMessage.createdAt.max())
                                    .from(qSubMessage)
                                    .where(qSubMessage.chatRoomId.eq(qChatMessage.chatRoomId))
                            )
                        )
                )
            )
            .fetch();

        if (ObjectUtils.isEmpty(latestMessageIds)) {
            return Map.of();
        }

        // 최신 메시지 ID로 메시지 내용 조회
        List<Tuple> latestMessages = queryFactory
            .select(
                qChatMessage.chatRoomId,
                qChatMessage.content
            )
            .from(qChatMessage)
            .where(qChatMessage.id.in(latestMessageIds))
            .fetch();

        Map<Long, String> result = new HashMap<>();
        for (Tuple tuple : latestMessages) {
            Long chatRoomId = tuple.get(qChatMessage.chatRoomId);
            String content = tuple.get(qChatMessage.content);
            if (chatRoomId != null && content != null) {
                result.put(chatRoomId, content);
            }
        }

        return result;
    }

    private BooleanExpression buildCursorCondition(
        QChatMessage qChatMessage,
        CursorDto cursor
    ) {
        if (ObjectUtils.isEmpty(cursor)) {
            return null;
        }

        return qChatMessage.createdAt.lt(cursor.getCreatedAt())
            .or(
                qChatMessage.createdAt.eq(cursor.getCreatedAt())
                    .and(qChatMessage.id.lt(cursor.getId()))
            );
    }
}
