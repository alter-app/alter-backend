package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.LoginWithPasswordRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.ManagerLoginWithPasswordUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.type.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("managerLoginWithPassword")
@RequiredArgsConstructor
@Transactional
public class ManagerLoginWithPassword implements ManagerLoginWithPasswordUseCase {

    private final UserQueryRepository userQueryRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public GenerateTokenResponseDto execute(LoginWithPasswordRequestDto request) {
        User user = userQueryRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_LOGIN_INFO));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_LOGIN_INFO);
        }

        if (!user.getRoles().contains(UserRole.ROLE_MANAGER)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return GenerateTokenResponseDto.of(authService.generateAuthorization(user, TokenScope.MANAGER));
    }
}
