package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUpdateUserStatusRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AdminActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.inbound.AdminUpdateUserStatusUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminUpdateUserStatus")
@RequiredArgsConstructor
@Transactional
public class AdminUpdateUserStatus implements AdminUpdateUserStatusUseCase {

    private final UserQueryRepository userQueryRepository;

    @Override
    public void execute(Long userId, AdminUpdateUserStatusRequestDto request, AdminActor actor) {
        User user = userQueryRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));

        try {
            user.updateStatus(request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.CONFLICT, "현재 상태가 변경하고자 하는 상태와 동일합니다.");
        }
    }
}
