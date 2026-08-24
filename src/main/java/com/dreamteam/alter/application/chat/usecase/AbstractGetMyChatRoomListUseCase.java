package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.ChatRoomCursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatRoomListWithOpponentResponse;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.result.ChatRoomListResult;
import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import com.dreamteam.alter.domain.common.pagination.CursorPageQuery;
import com.dreamteam.alter.domain.common.pagination.CursorPageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public abstract class AbstractGetMyChatRoomListUseCase<A> extends AbstractChatUseCase {

    protected final ChatRoomQueryRepository chatRoomQueryRepository;
    protected final ChatMessageQueryRepository chatMessageQueryRepository;
    protected final ObjectMapper objectMapper;

    public final CursorPageResult<ChatRoomListResult> execute(
        A actor,
        CursorPageQuery query
    ) {
        TokenScope participantScope = getParticipantScope(actor);
        Long participantId = getParticipantId(actor);

        int totalCount = (int) chatRoomQueryRepository.countChatRoomsByParticipant(participantId, participantScope);
        if (totalCount == 0) {
            return CursorPageResult.empty(query.pageSize(), totalCount);
        }

        ChatRoomCursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(query.cursor())) {
            cursorDto = CursorUtil.decodeCursor(query.cursor(), ChatRoomCursorDto.class, objectMapper);
        }
        CursorPageRequest<ChatRoomCursorDto> cursorPageRequest =
            CursorPageRequest.of(cursorDto, query.pageSize());

        List<ChatRoomListWithOpponentResponse> chatRooms = chatRoomQueryRepository.getChatRoomListWithOpponent(
            participantId,
            participantScope,
            cursorPageRequest
        );

        if (ObjectUtils.isEmpty(chatRooms)) {
            return CursorPageResult.empty(query.pageSize(), totalCount);
        }

        // 채팅방 ID 목록 추출
        List<Long> chatRoomIds = chatRooms.stream()
            .map(ChatRoomListWithOpponentResponse::getId)
            .toList();

        // 최신 메시지 내용 일괄 조회 (N+1 방지)
        Map<Long, String> latestMessageContents = chatMessageQueryRepository.getLatestMessageContentsByChatRoomIds(chatRoomIds);

        chatRooms.forEach(chatRoom ->
            chatRoom.setLatestMessageContent(latestMessageContents.getOrDefault(chatRoom.getId(), null))
        );

        // Result 변환
        List<ChatRoomListResult> chatRoomList = chatRooms.stream()
            .map(this::toResult)
            .toList();

        // 커서 생성 (updatedAt 기준)
        ChatRoomListWithOpponentResponse last = chatRooms.getLast();
        String nextCursor = CursorUtil.encodeCursor(new ChatRoomCursorDto(last.getId(), last.getUpdatedAt()), objectMapper);

        return CursorPageResult.of(nextCursor, query.pageSize(), totalCount, chatRoomList);
    }

    protected abstract TokenScope getParticipantScope(A actor);

    protected abstract Long getParticipantId(A actor);

    private ChatRoomListResult toResult(ChatRoomListWithOpponentResponse chatRoom) {
        boolean isGroup = ChatRoomType.GROUP.equals(chatRoom.getType());

        String opponentName = chatRoom.getOpponentName();
        String opponentProfileImageUrl = chatRoom.getOpponentProfileImageUrl();
        String roomName;

        if (isGroup) {
            roomName = ObjectUtils.isNotEmpty(chatRoom.getWorkspaceName()) ? chatRoom.getWorkspaceName() : "알 수 없음";
        } else {
            if (ObjectUtils.isEmpty(opponentName)) {
                // opponentName이 없으면(상대가 비활성 상태) 프로필 이미지도 노출하지 않는다
                opponentName = "알 수 없음";
                opponentProfileImageUrl = null;
            }
            roomName = opponentName;
        }

        return ChatRoomListResult.builder()
            .id(chatRoom.getId())
            .type(chatRoom.getType())
            .roomName(roomName)
            .memberCount(chatRoom.getMemberCount().intValue())
            .opponentId(chatRoom.getOpponentId())
            .opponentScope(chatRoom.getOpponentScope())
            .opponentName(opponentName)
            .opponentProfileImageUrl(opponentProfileImageUrl)
            .latestMessageContent(chatRoom.getLatestMessageContent())
            .createdAt(chatRoom.getCreatedAt())
            .updatedAt(chatRoom.getUpdatedAt())
            .build();
    }
}
