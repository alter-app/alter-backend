package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.general.chat.dto.SendChatMessageRequestDto;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;
import com.dreamteam.alter.application.chat.event.ChatMessageSentEvent;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatMessage;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.type.ChatMessageType;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomRepository;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.adapter.inbound.common.dto.FileResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional
public abstract class AbstractSendChatMessageUseCase<U> extends AbstractChatUseCase {

    protected final ChatRoomQueryRepository chatRoomQueryRepository;
    protected final ChatRoomRepository chatRoomRepository;
    protected final ChatMessageRepository chatMessageRepository;
    protected final AttachFilesUseCase attachFilesUseCase;
    protected final FileQueryRepository fileQueryRepository;
    protected final FileUrlService fileUrlService;
    protected final ApplicationEventPublisher eventPublisher;

    public final void execute(U user, SendChatMessageRequestDto request, Long chatRoomId) {
        TokenScope senderScope = getParticipantScope(user);
        Long senderId = getParticipantId(user);

        // 1. 채팅방 존재 확인 및 참여자 검증 (활성 멤버 EXISTS 기준)
        ChatRoom chatRoom = chatRoomQueryRepository.findByIdAndParticipant(chatRoomId, senderId, senderScope)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        // 2. NOTICE 권한 검증 (매니저만 공지 작성 가능)
        ChatMessageType type = request.getType() == null ? ChatMessageType.NORMAL : request.getType();
        if (type == ChatMessageType.NOTICE && senderScope != TokenScope.MANAGER) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "공지는 매니저만 작성할 수 있습니다.");
        }

        // 2-1. 내용/첨부 검증
        //   - fileIds: blank 원소 제거 후 중복 제거
        //   - content: 공백-only는 미입력으로 간주(isBlank), 길이 상한 검사로 DB 제약 위반(500) 대신 400 반환
        List<String> fileIds = request.getFileIds() == null
            ? List.of()
            : request.getFileIds().stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        boolean hasText = StringUtils.isNotBlank(request.getContent());
        if (!hasText && fileIds.isEmpty()) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "내용 또는 이미지를 첨부해야 합니다.");
        }
        if (hasText && request.getContent().length() > SendChatMessageRequestDto.MAX_CONTENT_LENGTH) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "메시지는 최대 1000자까지 입력할 수 있습니다.");
        }
        if (fileIds.size() > SendChatMessageRequestDto.MAX_ATTACHMENTS) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "이미지는 최대 10개까지 첨부할 수 있습니다.");
        }

        // 3. 메시지 저장
        ChatMessage chatMessage = ChatMessage.create(
            chatRoom.getId(),
            senderId,
            senderScope,
            type,
            request.getContent()
        );
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // 3-1. 첨부 파일 연결
        List<FileResponseDto> attachments = List.of();
        if (!fileIds.isEmpty()) {
            attachFilesUseCase.execute(
                fileIds,
                FileTargetType.CHAT_MESSAGE,
                String.valueOf(savedMessage.getId()),
                senderId
            );
            attachments = fileQueryRepository
                .findAllByTargetTypeAndTargetIdIn(FileTargetType.CHAT_MESSAGE, List.of(String.valueOf(savedMessage.getId())))
                .stream()
                .map(fileUrlService::resolve)
                .toList();
        }

        // 4. ChatRoom의 updatedAt 갱신
        chatRoom.updateUpdatedAt();

        // 5. 실시간 전송/FCM은 커밋 이후(AFTER_COMMIT) 리스너에서 처리한다.
        ChatMessageResponse messageResponse = new ChatMessageResponse(
            savedMessage.getId(),
            savedMessage.getChatRoomId(),
            savedMessage.getSenderId(),
            savedMessage.getSenderScope(),
            getParticipantName(user),
            savedMessage.getType(),
            savedMessage.getContent(),
            savedMessage.getCreatedAt()
        ).withFiles(
            attachments,
            fileUrlService.resolveUrlByTarget(FileTargetType.USER_PROFILE, String.valueOf(senderId))
        );
        eventPublisher.publishEvent(
            new ChatMessageSentEvent(chatRoom, senderId, senderScope, request.getContent(), messageResponse)
        );
    }

    protected abstract TokenScope getParticipantScope(U user);

    protected abstract Long getParticipantId(U user);

    protected abstract String getParticipantName(U user);

}
