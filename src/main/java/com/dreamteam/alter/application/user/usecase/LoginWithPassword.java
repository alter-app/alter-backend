package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.LoginWithPasswordRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.domain.auth.entity.AuthLog;
import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.auth.port.outbound.AuthLogRepository;
import com.dreamteam.alter.domain.auth.type.AuthLogType;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.port.inbound.LoginWithPasswordUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("loginWithPassword")
@RequiredArgsConstructor
@Transactional
public class LoginWithPassword implements LoginWithPasswordUseCase {

    private final UserQueryRepository userQueryRepository;
    private final AuthService authService;
    private final AuthLogRepository authLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public GenerateTokenResponseDto execute(LoginWithPasswordRequestDto request) {
        User user = userQueryRepository.findByContact(request.getContact())
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_LOGIN_INFO));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_LOGIN_INFO);
        }

        switch (user.getStatus()) {
            case SUSPENDED -> throw new CustomException(ErrorCode.SUSPENDED_USER);
            case DELETED -> throw new CustomException(ErrorCode.DELETED_USER);
        }

        // 기존 인가 정보 정리
        authService.revokeAllExistingAuthorizations(user);

        TokenScope scope = switch (user.getRole()) {
            case ROLE_MANAGER -> TokenScope.MANAGER;
            case ROLE_ADMIN -> TokenScope.ADMIN;
            default -> TokenScope.APP;
        };

        Authorization authorization = authService.generateAuthorization(user, scope);
        authLogRepository.save(AuthLog.create(user, authorization, AuthLogType.LOGIN));

        return GenerateTokenResponseDto.of(authorization);
    }
}
