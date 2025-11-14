package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatMessageResponseDto;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public abstract class AbstractGetChatMessagesUseCase<A> extends AbstractChatUseCase {

    protected final ChatRoomQueryRepository chatRoomQueryRepository;
    protected final ChatMessageQueryRepository chatMessageQueryRepository;
    protected final ObjectMapper objectMapper;

    protected CursorPaginatedApiResponse<ChatMessageResponseDto> execute(
        A actor,
        Long chatRoomId,
        CursorPageRequestDto pageRequest
    ) {
        TokenScope participantScope = getParticipantScope(actor);
        Long participantId = getParticipantId(actor);

        // 1. 채팅방 존재 확인 및 참여자 검증
        chatRoomQueryRepository.findByIdAndParticipant(chatRoomId, participantId, participantScope)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        // 2. 커서 디코딩
        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(pageRequest.cursor())) {
            cursorDto = CursorUtil.decodeCursor(pageRequest.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> cursorPageRequest = CursorPageRequest.of(cursorDto, pageRequest.pageSize());

        // 3. 메시지 목록 조회 (내림차순으로 조회)
        List<ChatMessageResponse> messages = chatMessageQueryRepository.getChatMessagesWithCursor(
            chatRoomId,
            cursorPageRequest
        );

        if (ObjectUtils.isEmpty(messages)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequest.pageSize(), 0));
        }

        // 4. 클라이언트 표시 순서에 맞게 오름차순으로 변환 (가장 오래된 메시지가 첫 번째)
        List<ChatMessageResponse> sortedMessages = messages.reversed();

        // 5. DTO 변환 (본인 메시지 여부 포함)
        List<ChatMessageResponseDto> messageList = sortedMessages.stream()
            .map(message -> ChatMessageResponseDto.from(message, participantId, participantScope))
            .toList();

        // 6. 커서 생성 (가장 오래된 메시지 기준으로 설정하여 다음 페이지 조회 시 이전 메시지 조회)
        ChatMessageResponse oldestMessage = sortedMessages.getFirst();
        CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new CursorDto(oldestMessage.getId(), oldestMessage.getCreatedAt()), objectMapper),
            pageRequest.pageSize(),
            messages.size()
        );

        return CursorPaginatedApiResponse.of(pageResponseDto, messageList);
    }

    protected abstract TokenScope getParticipantScope(A actor);

    protected abstract Long getParticipantId(A actor);
}
