package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.entity.AuthLog;
import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.auth.port.outbound.AuthLogRepository;
import com.dreamteam.alter.domain.auth.type.AuthLogType;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.email.port.outbound.EmailVerificationSessionStoreRepository;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateUserTx {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final AuthLogRepository authLogRepository;
    private final EmailVerificationSessionStoreRepository emailVerificationSessionStoreRepository;

    @Transactional
    public GenerateTokenResponseDto process(
        CreateUserRequestDto request,
        String contact,
        String verifiedEmail
    ) {
        // 사용자 생성
        User user = userRepository.save(User.create(
            contact,
            passwordEncoder.encode(request.getPassword()),
            request.getName(),
            request.getNickname(),
            request.getGender(),
            request.getBirthday(),
            verifiedEmail
        ));

        // 이메일 인증 세션 삭제
        if (ObjectUtils.isNotEmpty(verifiedEmail)) {
            emailVerificationSessionStoreRepository.deleteSession(request.getEmailSessionId());
        }

        Authorization authorization = authService.generateAuthorization(user, TokenScope.APP);
        authLogRepository.save(AuthLog.create(user, authorization, AuthLogType.LOGIN));

        return GenerateTokenResponseDto.of(authorization);
    }
}
