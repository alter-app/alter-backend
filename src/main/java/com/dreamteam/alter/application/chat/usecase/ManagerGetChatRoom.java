package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatRoomResponseDto;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.port.inbound.ManagerGetChatRoomUseCase;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("managerGetChatRoom")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerGetChatRoom extends AbstractChatUseCase implements ManagerGetChatRoomUseCase {

    private final ChatRoomQueryRepository chatRoomQueryRepository;
    private final ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;
    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final UserQueryRepository userQueryRepository;
    private final FileUrlService fileUrlService;

    @Override
    public ChatRoomResponseDto execute(ManagerActor actor, Long chatRoomId) {
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
            opponentName = opponentUser != null ? opponentUser.getName() : null;
            opponentProfileImageUrl = fileUrlService.resolveUrlByTarget(FileTargetType.USER_PROFILE, String.valueOf(opponentId));
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
}
