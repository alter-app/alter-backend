package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.application.auth.token.RefreshTokenAuthentication;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.auth.port.outbound.AuthorizationRepository;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.ReissueTokenUseCase;
import com.dreamteam.alter.domain.user.type.UserRole;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("reissueToken")
@RequiredArgsConstructor
public class ReissueToken implements ReissueTokenUseCase {

    private final AuthorizationRepository authorizationRepository;

    private final AuthService authService;

    @Override
    public GenerateTokenResponseDto execute(Authentication authentication) {
        if (BooleanUtils.isFalse(RefreshTokenAuthentication.class.isAssignableFrom(authentication.getClass()))) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_REQUIRED);
        }

        Authorization authorization = (Authorization) authentication.getDetails();
        User user = authorization.getUser();

        switch (user.getStatus()) {
            case SUSPENDED -> throw new CustomException(ErrorCode.SUSPENDED_USER);
            case DELETED -> throw new CustomException(ErrorCode.DELETED_USER);
        }

        authorization.expire();
        authorizationRepository.save(authorization); // 영속성으로 관리되지 않으므로 직접 저장

        Authorization newAuthorization;
        try {
            newAuthorization = authService.generateAuthorization(user, authorization.getScope());
            authService.saveAuthorization(newAuthorization);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return GenerateTokenResponseDto.of(newAuthorization);
    }

}
