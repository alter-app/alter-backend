package com.dreamteam.alter.adapter.inbound.general.chat.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatMessageResponseDto;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatRoomListResponseDto;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatRoomResponseDto;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.CreateChatRoomRequestDto;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.CreateChatRoomResponseDto;
import com.dreamteam.alter.application.aop.AppActionContext;
import com.dreamteam.alter.domain.chat.port.inbound.CreateOrGetChatRoomUseCase;
import com.dreamteam.alter.domain.chat.port.inbound.GetChatMessagesUseCase;
import com.dreamteam.alter.domain.chat.port.inbound.GetChatRoomUseCase;
import com.dreamteam.alter.domain.chat.port.inbound.GetMyChatRoomListUseCase;
import com.dreamteam.alter.domain.user.context.AppActor;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/chat")
@PreAuthorize("hasAnyRole('USER')")
@RequiredArgsConstructor
@Validated
public class UserChatController implements UserChatControllerSpec {

    @Resource(name = "createOrGetChatRoom")
    private final CreateOrGetChatRoomUseCase createOrGetChatRoomUseCase;

    @Resource(name = "getMyChatRoomList")
    private final GetMyChatRoomListUseCase getMyChatRoomListUseCase;

    @Resource(name = "getChatMessages")
    private final GetChatMessagesUseCase getChatMessagesUseCase;

    @Resource(name = "getChatRoom")
    private final GetChatRoomUseCase getChatRoomUseCase;

    @Override
    @PostMapping("/rooms")
    public ResponseEntity<CommonApiResponse<CreateChatRoomResponseDto>> createOrGetChatRoom(
        @RequestBody CreateChatRoomRequestDto request
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        CreateChatRoomResponseDto response =
            createOrGetChatRoomUseCase.execute(actor, request.getOpponentUserId(), request.getOpponentScope());
        return ResponseEntity.ok(CommonApiResponse.of(response));
    }

    @Override
    @GetMapping("/rooms")
    public ResponseEntity<CursorPaginatedApiResponse<ChatRoomListResponseDto>> getMyChatRoomList(
        CursorPageRequestDto pageRequest
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        return ResponseEntity.ok(getMyChatRoomListUseCase.execute(actor, pageRequest));
    }

    @Override
    @GetMapping("/rooms/{chatRoomId}")
    public ResponseEntity<CommonApiResponse<ChatRoomResponseDto>> getChatRoom(
        @PathVariable Long chatRoomId
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(getChatRoomUseCase.execute(actor, chatRoomId)));
    }

    @Override
    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<CursorPaginatedApiResponse<ChatMessageResponseDto>> getChatMessages(
        @PathVariable Long chatRoomId,
        CursorPageRequestDto pageRequest
    ) {
        AppActor actor = AppActionContext.getInstance().getActor();
        return ResponseEntity.ok(getChatMessagesUseCase.execute(actor, chatRoomId, pageRequest));
    }
}
