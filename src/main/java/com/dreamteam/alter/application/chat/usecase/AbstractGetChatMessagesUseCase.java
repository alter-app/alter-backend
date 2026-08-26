package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.adapter.inbound.common.dto.FileResponseDto;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.result.ChatAttachmentResult;
import com.dreamteam.alter.domain.chat.result.ChatMessageResult;
import com.dreamteam.alter.domain.common.pagination.CursorPageQuery;
import com.dreamteam.alter.domain.common.pagination.CursorPageResult;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public abstract class AbstractGetChatMessagesUseCase<A> extends AbstractChatUseCase {

    protected final ChatRoomQueryRepository chatRoomQueryRepository;
    protected final ChatMessageQueryRepository chatMessageQueryRepository;
    protected final ObjectMapper objectMapper;
    protected final ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;
    protected final FileQueryRepository fileQueryRepository;
    protected final FileUrlService fileUrlService;

    protected CursorPageResult<ChatMessageResult> execute(
        A actor,
        Long chatRoomId,
        CursorPageQuery query
    ) {
        TokenScope participantScope = getParticipantScope(actor);
        Long participantId = getParticipantId(actor);

        // 1. 채팅방 존재 확인 및 참여자 검증 (활성 멤버 EXISTS 기준)
        ChatRoom chatRoom = chatRoomQueryRepository.findByIdAndParticipant(chatRoomId, participantId, participantScope)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        // 2. 커서 디코딩
        CursorDto cursorDto = null;
        if (ObjectUtils.isNotEmpty(query.cursor())) {
            cursorDto = CursorUtil.decodeCursor(query.cursor(), CursorDto.class, objectMapper);
        }
        CursorPageRequest<CursorDto> cursorPageRequest = CursorPageRequest.of(cursorDto, query.pageSize());

        // 3. 메시지 목록 조회 (내림차순으로 조회)
        List<ChatMessageResponse> messages = chatMessageQueryRepository.getChatMessagesWithCursor(
            chatRoomId,
            cursorPageRequest
        );

        if (ObjectUtils.isEmpty(messages)) {
            return CursorPageResult.empty(query.pageSize(), 0);
        }

        // 4. 클라이언트 표시 순서에 맞게 오름차순으로 변환 (가장 오래된 메시지가 첫 번째)
        List<ChatMessageResponse> sortedMessages = messages.reversed();

        // 5. 방의 활성 멤버 목록을 한 번만 로드 (메시지별 안 읽은 사람 수 계산용, N+1 방지)
        List<ChatRoomMember> activeMembers = chatRoomMemberQueryRepository.findActiveByRoom(chatRoomId);

        // 5-1. 페이지 내 메시지들의 첨부 파일과 발신자 프로필 이미지를 한 번에 조회하여 매핑 (N+1 방지)
        List<ChatMessageResponse> resolvedMessages = resolveFiles(sortedMessages);

        // 6. Result 변환 (본인 메시지 여부 + 안 읽은 사람 수 포함)
        List<ChatMessageResult> messageList = resolvedMessages.stream()
            .map(message -> toResult(message, participantId, participantScope, activeMembers))
            .toList();

        // 7. 커서 생성 (가장 오래된 메시지 기준으로 설정하여 다음 페이지 조회 시 이전 메시지 조회)
        ChatMessageResponse oldestMessage = resolvedMessages.getFirst();
        String nextCursor = CursorUtil.encodeCursor(new CursorDto(oldestMessage.getId(), oldestMessage.getCreatedAt()), objectMapper);

        return CursorPageResult.of(nextCursor, query.pageSize(), messages.size(), messageList);
    }

    protected abstract TokenScope getParticipantScope(A actor);

    protected abstract Long getParticipantId(A actor);

    // 메시지의 첨부 파일과 발신자 프로필 이미지를 각각 한 번씩만 조회해 매핑한 새 목록을 반환한다 (N+1 방지)
    private List<ChatMessageResponse> resolveFiles(List<ChatMessageResponse> messages) {
        Map<String, List<FileResponseDto>> attachmentsByMessageId = findAttachments(messages);
        Map<String, String> profileImageUrlBySenderId = findSenderProfileImageUrls(messages);

        return messages.stream()
            .map(message -> message.withFiles(
                attachmentsByMessageId.getOrDefault(String.valueOf(message.getId()), Collections.emptyList()),
                profileImageUrlBySenderId.get(String.valueOf(message.getSenderId()))
            ))
            .toList();
    }

    // targetId(=메시지 id) 기준으로 첨부 파일 그룹핑
    private Map<String, List<FileResponseDto>> findAttachments(List<ChatMessageResponse> messages) {
        List<String> messageIds = messages.stream()
            .map(message -> String.valueOf(message.getId()))
            .toList();

        List<File> files = fileQueryRepository.findAllByTargetTypeAndTargetIdIn(FileTargetType.CHAT_MESSAGE, messageIds);

        return files.stream()
            .collect(Collectors.groupingBy(
                File::getTargetId,
                Collectors.mapping(fileUrlService::resolve, Collectors.toList())
            ));
    }

    // targetId(=발신자 User id) 기준으로 프로필 이미지 URL 매핑
    private Map<String, String> findSenderProfileImageUrls(List<ChatMessageResponse> messages) {
        List<String> senderIds = messages.stream()
            .map(message -> String.valueOf(message.getSenderId()))
            .distinct()
            .toList();

        List<File> files = fileQueryRepository.findAllByTargetTypeAndTargetIdIn(FileTargetType.USER_PROFILE, senderIds);

        return files.stream()
            .collect(Collectors.toMap(
                File::getTargetId,
                file -> fileUrlService.resolve(file).getUrl(),
                (first, second) -> first
            ));
    }

    private ChatMessageResult toResult(
        ChatMessageResponse message,
        Long participantId,
        TokenScope participantScope,
        List<ChatRoomMember> activeMembers
    ) {
        boolean isMine = message.getSenderId().equals(participantId)
            && message.getSenderScope().equals(participantScope);

        return ChatMessageResult.builder()
            .id(message.getId())
            .senderId(message.getSenderId())
            .senderScope(message.getSenderScope())
            .senderName(message.getSenderName())
            .senderProfileImageUrl(message.getSenderProfileImageUrl())
            .type(message.getType())
            .content(message.getContent())
            .createdAt(message.getCreatedAt())
            .isMine(isMine)
            .unreadCount(calculateUnreadCount(message, activeMembers))
            .attachments(toAttachmentResults(message.getAttachments()))
            .build();
    }

    // FileResponseDto(adapter DTO) → ChatAttachmentResult(도메인 결과) 변환은 application 계층 책임
    private List<ChatAttachmentResult> toAttachmentResults(List<FileResponseDto> attachments) {
        return attachments.stream()
            .map(file -> new ChatAttachmentResult(file.getFileId(), file.getUrl()))
            .toList();
    }

    // 메시지별 안 읽은 사람 수 = 활성 멤버 중 (아직 읽지 않았고) AND (발신자 본인이 아닌) 인원 수
    private int calculateUnreadCount(ChatMessageResponse message, List<ChatRoomMember> activeMembers) {
        return (int) activeMembers.stream()
            .filter(member -> hasNotRead(member, message))
            .filter(member -> !isSender(member, message))
            .count();
    }

    private boolean hasNotRead(ChatRoomMember member, ChatMessageResponse message) {
        return member.getLastReadMessageId() == null || member.getLastReadMessageId() < message.getId();
    }

    private boolean isSender(ChatRoomMember member, ChatMessageResponse message) {
        return member.getMemberId().equals(message.getSenderId())
            && member.getMemberScope().equals(message.getSenderScope());
    }
}
