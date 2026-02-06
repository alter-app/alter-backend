package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUpdateUserStatusRequestDto;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AdminActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.AdminUpdateUserStatusUseCase;
import com.dreamteam.alter.domain.user.port.outbound.AdminUserQueryRepository;
import com.dreamteam.alter.domain.user.type.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminUpdateUserStatus")
@RequiredArgsConstructor
@Transactional
public class AdminUpdateUserStatus implements AdminUpdateUserStatusUseCase {

    private final AdminUserQueryRepository adminUserQueryRepository;
    private final AuthService authService;

    @Override
    public void execute(Long userId, AdminUpdateUserStatusRequestDto request, AdminActor actor) {
        if (UserStatus.DELETED.equals(request.getStatus())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "변경 가능한 상태가 아닙니다.");
        }

        User user = adminUserQueryRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));

        try {
            user.updateStatus(request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.CONFLICT, "현재 상태가 변경하고자 하는 상태와 동일합니다.");
        }

        if (UserStatus.SUSPENDED.equals(request.getStatus())) {
            authService.revokeAllExistingAuthorizations(user);
        }
    }
}
