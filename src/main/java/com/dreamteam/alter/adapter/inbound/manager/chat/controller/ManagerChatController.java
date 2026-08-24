package com.dreamteam.alter.adapter.inbound.manager.chat.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatMessageResponseDto;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatRoomListResponseDto;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatRoomResponseDto;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.CreateChatRoomRequestDto;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.CreateChatRoomResponseDto;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.MarkChatRoomReadRequestDto;
import com.dreamteam.alter.application.aop.ManagerActionContext;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.inbound.GetWorkspaceGroupChatRoomUseCase;
import com.dreamteam.alter.domain.chat.port.inbound.ManagerCreateOrGetChatRoomUseCase;
import com.dreamteam.alter.domain.chat.port.inbound.ManagerGetChatMessagesUseCase;
import com.dreamteam.alter.domain.chat.port.inbound.ManagerGetChatRoomUseCase;
import com.dreamteam.alter.domain.chat.port.inbound.ManagerGetMyChatRoomListUseCase;
import com.dreamteam.alter.domain.chat.port.inbound.MarkChatRoomReadUseCase;
import com.dreamteam.alter.domain.chat.result.ChatMessageResult;
import com.dreamteam.alter.domain.chat.result.ChatRoomListResult;
import com.dreamteam.alter.domain.common.pagination.CursorPageQuery;
import com.dreamteam.alter.domain.common.pagination.CursorPageResult;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Function;

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

    @Resource(name = "markChatRoomRead")
    private final MarkChatRoomReadUseCase markChatRoomRead;

    @Resource(name = "getWorkspaceGroupChatRoom")
    private final GetWorkspaceGroupChatRoomUseCase getWorkspaceGroupChatRoom;

    @Override
    @PostMapping("/rooms")
    public ResponseEntity<CommonApiResponse<CreateChatRoomResponseDto>> createOrGetChatRoom(
        @RequestBody CreateChatRoomRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        CreateChatRoomResponseDto response = CreateChatRoomResponseDto.from(
            managerCreateOrGetChatRoomUseCase.execute(actor, request.getOpponentUserId(), request.getOpponentScope())
        );
        return ResponseEntity.ok(CommonApiResponse.of(response));
    }

    @Override
    @GetMapping("/rooms")
    public ResponseEntity<CursorPaginatedApiResponse<ChatRoomListResponseDto>> getMyChatRoomList(
        CursorPageRequestDto pageRequest
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        CursorPageResult<ChatRoomListResult> result = managerGetMyChatRoomListUseCase.execute(
            actor, CursorPageQuery.of(pageRequest.cursor(), pageRequest.pageSize()));
        return ResponseEntity.ok(toPaginatedResponse(result, ChatRoomListResponseDto::from));
    }

    @Override
    @GetMapping("/rooms/{chatRoomId}")
    public ResponseEntity<CommonApiResponse<ChatRoomResponseDto>> getChatRoom(
        @PathVariable Long chatRoomId
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        return ResponseEntity.ok(CommonApiResponse.of(ChatRoomResponseDto.from(managerGetChatRoomUseCase.execute(actor, chatRoomId))));
    }

    @Override
    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<CursorPaginatedApiResponse<ChatMessageResponseDto>> getChatMessages(
        @PathVariable Long chatRoomId,
        CursorPageRequestDto pageRequest
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        CursorPageResult<ChatMessageResult> result = managerGetChatMessagesUseCase.execute(
            actor, chatRoomId, CursorPageQuery.of(pageRequest.cursor(), pageRequest.pageSize()));
        return ResponseEntity.ok(toPaginatedResponse(result, ChatMessageResponseDto::from));
    }

    @Override
    @PostMapping("/rooms/{chatRoomId}/read")
    public ResponseEntity<CommonApiResponse<Void>> markChatRoomRead(
        @PathVariable Long chatRoomId,
        @Valid @RequestBody MarkChatRoomReadRequestDto request
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        markChatRoomRead.execute(actor.getUserId(), TokenScope.MANAGER, chatRoomId, request.getLastReadMessageId());
        return ResponseEntity.ok(CommonApiResponse.empty());
    }

    @Override
    @GetMapping("/workspace/{workspaceId}/room")
    public ResponseEntity<CommonApiResponse<CreateChatRoomResponseDto>> getWorkspaceGroupChatRoom(
        @PathVariable Long workspaceId
    ) {
        ManagerActor actor = ManagerActionContext.getInstance().getActor();
        Long roomId = getWorkspaceGroupChatRoom.execute(actor.getUserId(), TokenScope.MANAGER, workspaceId);
        return ResponseEntity.ok(CommonApiResponse.of(CreateChatRoomResponseDto.of(roomId)));
    }
    // CursorPageResult(domain) → CursorPaginatedApiResponse(응답 포맷) 정규화는 컨트롤러 책임
    private static <T, R> CursorPaginatedApiResponse<R> toPaginatedResponse(
        CursorPageResult<T> result,
        Function<T, R> mapper
    ) {
        return CursorPaginatedApiResponse.of(
            CursorPageResponseDto.of(result.nextCursor(), result.pageSize(), result.totalCount()),
            result.data().stream().map(mapper).toList()
        );
    }
}
