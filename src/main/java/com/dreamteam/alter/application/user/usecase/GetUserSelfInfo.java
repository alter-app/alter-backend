package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.general.user.dto.UserSelfInfoResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.port.inbound.GetUserSelfInfoUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("getUserSelfInfo")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserSelfInfo implements GetUserSelfInfoUseCase {

    private final UserQueryRepository userQueryRepository;

    @Override
    public UserSelfInfoResponseDto execute(AppActor actor) {
        return UserSelfInfoResponseDto.from(userQueryRepository.getUserSelfInfoSummary(actor.getUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)));
    }

}
