package com.dreamteam.alter.adapter.inbound.general.chat.controller;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.LoginUserDto;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.SendChatMessageRequestDto;
import com.dreamteam.alter.application.auth.token.AccessTokenAuthentication;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.chat.port.inbound.SendChatMessageUseCase;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
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
public class UserChatWebSocketController implements UserChatWebSocketControllerSpec {

    @Resource(name = "sendChatMessage")
    private final SendChatMessageUseCase sendChatMessageUseCase;
    private final UserQueryRepository userQueryRepository;

    @Override
    @MessageMapping("/app/send.{chatRoomId}")
    public void sendMessage(
        @Payload SendChatMessageRequestDto request,
        @DestinationVariable Long chatRoomId,
        Principal principal
    ) {
        if (principal instanceof Authentication authentication
            && AccessTokenAuthentication.class.isAssignableFrom(authentication.getClass())) {
            LoginUserDto loginUser = (LoginUserDto) authentication.getDetails();

            User user = userQueryRepository.findById(loginUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            sendChatMessageUseCase.execute(user, request, chatRoomId);
        }
    }
}
