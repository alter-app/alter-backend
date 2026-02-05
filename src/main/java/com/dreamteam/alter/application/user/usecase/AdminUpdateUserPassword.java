package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUpdateUserPasswordRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.util.PasswordValidator;
import com.dreamteam.alter.domain.user.context.AdminActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.AdminUpdateUserPasswordUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminUpdateUserPassword")
@RequiredArgsConstructor
@Transactional
public class AdminUpdateUserPassword implements AdminUpdateUserPasswordUseCase {

    private final UserQueryRepository userQueryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void execute(Long userId, AdminUpdateUserPasswordRequestDto request, AdminActor actor) {
        // 사용자 조회
        User user = userQueryRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 형식 검증
        if (!PasswordValidator.isValid(request.getNewPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }

        // 비밀번호 업데이트
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }
}
