package com.dreamteam.alter.adapter.inbound.manager.chat.controller;

import com.dreamteam.alter.adapter.inbound.general.chat.dto.SendChatMessageRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Payload;

import java.security.Principal;

@Tag(name = "MANAGER - 채팅 WebSocket API")
@Schema(description = "WebSocket을 통한 실시간 채팅 메시지 전송. STOMP 프로토콜을 사용하며, 연결은 /ws 엔드포인트를 통해 이루어집니다.")
public interface ManagerChatWebSocketControllerSpec {

    @Operation(
        summary = "채팅 메시지 전송 (WebSocket)",
        description = "WebSocket을 통해 채팅 메시지를 전송합니다. " +
            "STOMP 메시지 브로커를 사용하며, 클라이언트는 /manager/chat/send로 메시지를 보내야 합니다. " +
            "구독 경로는 /topic/chat/{chatRoomId} 입니다. " +
            "실제 사용 시 WebSocket 클라이언트를 통해 연결해야 합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "메시지 전송 성공 (WebSocket은 응답이 없으며, 구독자에게 브로드캐스트됩니다)")
    })
    void sendMessage(@Payload SendChatMessageRequestDto request, @DestinationVariable Long chatRoomId, Principal principal);
}
