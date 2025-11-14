package com.dreamteam.alter.adapter.inbound.manager.chat.controller;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.LoginUserDto;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.SendChatMessageRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.chat.port.inbound.ManagerSendChatMessageUseCase;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.port.outbound.ManagerUserQueryRepository;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ManagerChatWebSocketController implements ManagerChatWebSocketControllerSpec {

    @Resource(name = "managerSendChatMessage")
    private final ManagerSendChatMessageUseCase managerSendChatMessageUseCase;
    private final ManagerUserQueryRepository managerUserQueryRepository;

    @Override
    @MessageMapping("/manager/send.{chatRoomId}")
    public void sendMessage(
        @Payload SendChatMessageRequestDto request,
        @DestinationVariable Long chatRoomId,
        Principal principal
    ) {
        if (principal instanceof Authentication authentication
            && authentication.getDetails() instanceof LoginUserDto loginUser) {
            ManagerUser managerUser = managerUserQueryRepository.findByUserId(loginUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            managerSendChatMessageUseCase.execute(managerUser, request, chatRoomId);
        }
    }
}
