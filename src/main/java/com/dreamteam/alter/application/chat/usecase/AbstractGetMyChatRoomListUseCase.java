package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.ChatRoomCursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatRoomListResponseDto;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatRoomListWithOpponentResponse;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public abstract class AbstractGetMyChatRoomListUseCase<A> extends AbstractChatUseCase {

    protected final ChatRoomQueryRepository chatRoomQueryRepository;
    protected final ChatMessageQueryRepository chatMessageQueryRepository;
    protected final ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;
    protected final FileQueryRepository fileQueryRepository;
    protected final FileUrlService fileUrlService;
    protected final ObjectMapper objectMapper;

    public final CursorPaginatedApiResponse<ChatRoomListResponseDto> execute(
        A actor,
        CursorPageRequestDto pageRequest
    ) {
        TokenScope participantScope = getParticipantScope(actor);
        Long participantId = getParticipantId(actor);

        int totalCount = (int) chatRoomQueryRepository.countChatRoomsByParticipant(participantId, participantScope);
        if (totalCount == 0) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequest.pageSize(), totalCount));
        }

        ChatRoomCursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(pageRequest.cursor())) {
            cursorDto = CursorUtil.decodeCursor(pageRequest.cursor(), ChatRoomCursorDto.class, objectMapper);
        }
        CursorPageRequest<ChatRoomCursorDto> cursorPageRequest =
            CursorPageRequest.of(cursorDto, pageRequest.pageSize());

        List<ChatRoomListWithOpponentResponse> chatRooms = chatRoomQueryRepository.getChatRoomListWithOpponent(
            participantId,
            participantScope,
            cursorPageRequest
        );

        if (ObjectUtils.isEmpty(chatRooms)) {
            return CursorPaginatedApiResponse.empty(CursorPageResponseDto.empty(pageRequest.pageSize(), totalCount));
        }

        // 채팅방 ID 목록 추출
        List<Long> chatRoomIds = chatRooms.stream()
            .map(ChatRoomListWithOpponentResponse::getId)
            .toList();

        // 최신 메시지 내용 / 참여 인원 수 일괄 조회 (N+1 방지)
        Map<Long, String> latestMessageContents = chatMessageQueryRepository.getLatestMessageContentsByChatRoomIds(chatRoomIds);
        Map<Long, Long> memberCounts = chatRoomMemberQueryRepository.countActiveByRoomIds(chatRoomIds);
        Map<Long, String> opponentProfileImageUrls = getOpponentProfileImageUrls(chatRooms);

        chatRooms.forEach(chatRoom -> {
            chatRoom.setLatestMessageContent(latestMessageContents.getOrDefault(chatRoom.getId(), null));
            chatRoom.setMemberCount(memberCounts.getOrDefault(chatRoom.getId(), 0L).intValue());

            if (ChatRoomType.GROUP.equals(chatRoom.getType())) {
                return;
            }

            chatRoom.setOpponentProfileImageUrl(opponentProfileImageUrls.get(chatRoom.getOpponentId()));
            // opponentName이 null인 경우 기본값 설정
            if (chatRoom.getOpponentName() == null) {
                chatRoom.setOpponentName("알 수 없음");
            }
        });

        // DTO 변환
        List<ChatRoomListResponseDto> chatRoomList = chatRooms.stream()
            .map(ChatRoomListResponseDto::from)
            .toList();

        // 커서 생성 (updatedAt 기준)
        ChatRoomListWithOpponentResponse last = chatRooms.getLast();
        CursorPageResponseDto pageResponseDto = CursorPageResponseDto.of(
            CursorUtil.encodeCursor(new ChatRoomCursorDto(last.getId(), last.getUpdatedAt()), objectMapper),
            pageRequest.pageSize(),
            totalCount
        );

        return CursorPaginatedApiResponse.of(pageResponseDto, chatRoomList);
    }

    protected abstract TokenScope getParticipantScope(A actor);

    protected abstract Long getParticipantId(A actor);

    // 개인 채팅방 상대방들의 프로필 이미지를 한 번에 조회해 userId 기준으로 매핑 (N+1 방지)
    private Map<Long, String> getOpponentProfileImageUrls(List<ChatRoomListWithOpponentResponse> chatRooms) {
        List<String> opponentIds = chatRooms.stream()
            .filter(chatRoom -> !ChatRoomType.GROUP.equals(chatRoom.getType()))
            .map(ChatRoomListWithOpponentResponse::getOpponentId)
            .filter(ObjectUtils::isNotEmpty)
            .map(String::valueOf)
            .distinct()
            .toList();

        List<File> files = fileQueryRepository.findAllByTargetTypeAndTargetIdIn(FileTargetType.USER_PROFILE, opponentIds);

        return files.stream()
            .collect(Collectors.toMap(
                file -> Long.valueOf(file.getTargetId()),
                file -> fileUrlService.resolve(file).getUrl(),
                (existing, duplicated) -> existing
            ));
    }
}
