package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUserDetailResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AdminActor;
import com.dreamteam.alter.domain.user.port.inbound.AdminGetUserDetailUseCase;
import com.dreamteam.alter.domain.user.port.outbound.AdminUserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminGetUserDetail")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGetUserDetail implements AdminGetUserDetailUseCase {

    private final AdminUserQueryRepository adminUserQueryRepository;

    @Override
    public AdminUserDetailResponseDto execute(Long userId, AdminActor actor) {
        return AdminUserDetailResponseDto.from(adminUserQueryRepository.getUserDetail(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다.")));
    }
}
