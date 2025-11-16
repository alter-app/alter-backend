package com.dreamteam.alter.adapter.inbound.general.chat.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatMessageResponseDto;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatRoomListResponseDto;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatRoomResponseDto;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.CreateChatRoomRequestDto;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.CreateChatRoomResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "APP - 채팅 API")
public interface UserChatControllerSpec {

    @Operation(summary = "채팅방 생성 또는 조회", description = "상대방과의 채팅방을 생성하거나 기존 채팅방을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "채팅방 생성/조회 성공")
    })
    ResponseEntity<CommonApiResponse<CreateChatRoomResponseDto>> createOrGetChatRoom(
        @Valid @RequestBody CreateChatRoomRequestDto request
    );

    @Operation(summary = "내 채팅방 목록 조회 (커서 페이징)", description = "내가 참여한 채팅방 목록을 최신순으로 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공")
    })
    ResponseEntity<CursorPaginatedApiResponse<ChatRoomListResponseDto>> getMyChatRoomList(
        CursorPageRequestDto pageRequest
    );

    @Operation(summary = "채팅방 정보 조회", description = "특정 채팅방의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "채팅방 정보 조회 성공")
    })
    ResponseEntity<CommonApiResponse<ChatRoomResponseDto>> getChatRoomInfo(
        @PathVariable Long chatRoomId
    );

    @Operation(summary = "채팅 메시지 목록 조회 (커서 페이징)", description = "특정 채팅방의 메시지 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "메시지 목록 조회 성공")
    })
    ResponseEntity<CursorPaginatedApiResponse<ChatMessageResponseDto>> getChatMessages(
        @PathVariable Long chatRoomId,
        CursorPageRequestDto pageRequest
    );
}
