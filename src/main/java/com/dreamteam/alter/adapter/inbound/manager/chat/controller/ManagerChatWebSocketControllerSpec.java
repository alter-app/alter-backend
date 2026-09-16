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
@Schema(description = "WebSocket을 통한 실시간 채팅 메시지 전송. STOMP 프로토콜을 사용하며, 연결은 /api/ws-connect 엔드포인트를 통해 이루어집니다. SockJS는 미지원합니다.")
public interface ManagerChatWebSocketControllerSpec {

    @Operation(
        summary = "채팅 메시지 전송 (WebSocket)",
        description = "WebSocket을 통해 채팅 메시지를 전송합니다. " +
            "STOMP 메시지 브로커를 사용하며, 클라이언트는 /pub/manager/send.{chatRoomId}로 메시지를 보내야 합니다. " +
            "구독 경로는 /sub/chat.{chatRoomId} 입니다. " +
            "CONNECT 프레임 헤더에 Authorization: Bearer {accessToken}이 필수입니다. " +
            "이 API는 REST가 아니므로 Swagger 문서의 Try it out으로는 호출할 수 없습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "메시지 전송 성공 (WebSocket은 응답이 없으며, 구독자에게 브로드캐스트됩니다)")
    })
    void sendMessage(@Payload SendChatMessageRequestDto request, @DestinationVariable Long chatRoomId, Principal principal);
}
