package com.dreamteam.alter.adapter.inbound.manager.chat.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatMessageResponseDto;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatRoomListResponseDto;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatRoomResponseDto;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.CreateChatRoomRequestDto;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.CreateChatRoomResponseDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.chat.port.inbound.ManagerCreateOrGetChatRoomUseCase;
import com.dreamteam.alter.domain.chat.port.inbound.ManagerGetChatMessagesUseCase;
import com.dreamteam.alter.domain.chat.port.inbound.ManagerGetChatRoomUseCase;
import com.dreamteam.alter.domain.chat.port.inbound.ManagerGetMyChatRoomListUseCase;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manager/chat")
@PreAuthorize("hasAnyRole('MANAGER')")
@RequiredArgsConstructor
@Validated
public class ManagerChatController implements ManagerChatControllerSpec {

    @Resource(name = "managerCreateOrGetChatRoom")
    private final ManagerCreateOrGetChatRoomUseCase managerCreateOrGetChatRoomUseCase;

    @Resource(name = "managerGetMyChatRoomList")
    private final ManagerGetMyChatRoomListUseCase managerGetMyChatRoomListUseCase;

    @Resource(name = "managerGetChatMessages")
    private final ManagerGetChatMessagesUseCase managerGetChatMessagesUseCase;

    @Resource(name = "managerGetChatRoom")
    private final ManagerGetChatRoomUseCase managerGetChatRoomUseCase;

    @Override
    @PostMapping("/rooms")
    public ResponseEntity<CommonApiResponse<CreateChatRoomResponseDto>> createOrGetChatRoom(
        @RequestBody CreateChatRoomRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        CreateChatRoomResponseDto response = managerCreateOrGetChatRoomUseCase.execute(actor, request.getOpponentUserId(), request.getOpponentScope());
        return ResponseEntity.ok(CommonApiResponse.of(response));
    }

    @Override
    @GetMapping("/rooms")
    public ResponseEntity<CursorPaginatedApiResponse<ChatRoomListResponseDto>> getMyChatRoomList(
        CursorPageRequestDto pageRequest
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        return ResponseEntity.ok(managerGetMyChatRoomListUseCase.execute(actor, pageRequest));
    }

    @Override
    @GetMapping("/rooms/{chatRoomId}")
    public ResponseEntity<CommonApiResponse<ChatRoomResponseDto>> getChatRoom(
        @PathVariable Long chatRoomId
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(managerGetChatRoomUseCase.execute(actor, chatRoomId)));
    }

    @Override
    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<CursorPaginatedApiResponse<ChatMessageResponseDto>> getChatMessages(
        @PathVariable Long chatRoomId,
        CursorPageRequestDto pageRequest
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        return ResponseEntity.ok(managerGetChatMessagesUseCase.execute(actor, chatRoomId, pageRequest));
    }
}
