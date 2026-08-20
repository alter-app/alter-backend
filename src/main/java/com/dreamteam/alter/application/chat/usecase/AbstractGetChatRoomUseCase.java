package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatRoomResponseDto;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public abstract class AbstractGetChatRoomUseCase<A> extends AbstractChatUseCase {

    protected final ChatRoomQueryRepository chatRoomQueryRepository;
    protected final ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;
    protected final WorkspaceQueryRepository workspaceQueryRepository;
    protected final UserQueryRepository userQueryRepository;
    protected final FileUrlService fileUrlService;

    public final ChatRoomResponseDto execute(A actor, Long chatRoomId) {
        TokenScope participantScope = getParticipantScope(actor);
        Long participantId = getParticipantId(actor);

        // 1. 채팅방 존재 확인 및 참여자 검증
        ChatRoom chatRoom = chatRoomQueryRepository.findByIdAndParticipant(
                chatRoomId,
                participantId,
                participantScope
            )
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        int memberCount = chatRoomMemberQueryRepository.countActiveByRoom(chatRoomId);

        String roomName;
        Long opponentId = null;
        TokenScope opponentScope = null;
        String opponentName = null;
        String opponentProfileImageUrl = null;

        if (chatRoom.getType() == ChatRoomType.GROUP) {
            roomName = workspaceQueryRepository.findById(chatRoom.getWorkspaceId())
                .map(Workspace::getBusinessName)
                .orElse(null);
        } else {
            if (chatRoom.getParticipant1Id()
                .equals(participantId)
                && chatRoom.getParticipant1Scope()
                .equals(participantScope)) {
                opponentId = chatRoom.getParticipant2Id();
                opponentScope = chatRoom.getParticipant2Scope();
            } else {
                opponentId = chatRoom.getParticipant1Id();
                opponentScope = chatRoom.getParticipant1Scope();
            }

            User opponentUser = userQueryRepository.findById(opponentId)
                .orElse(null);
            if (opponentUser != null) {
                opponentName = opponentUser.getName();
                opponentProfileImageUrl = fileUrlService.resolveUrlByTarget(FileTargetType.USER_PROFILE, String.valueOf(opponentId));
            } else {
                opponentName = "알 수 없음";
            }
            roomName = opponentName;
        }

        return ChatRoomResponseDto.of(
            chatRoom,
            memberCount,
            roomName,
            opponentId,
            opponentScope,
            opponentName,
            opponentProfileImageUrl
        );
    }

    protected abstract TokenScope getParticipantScope(A actor);

    protected abstract Long getParticipantId(A actor);
}
