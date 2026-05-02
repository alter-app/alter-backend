package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.command.GetUserSelfInfoCommand;
import com.dreamteam.alter.domain.user.port.inbound.GetUserSelfInfoUseCase;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.result.GetUserSelfInfoResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("getUserSelfInfo")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserSelfInfo implements GetUserSelfInfoUseCase {

    private final UserQueryRepository userQueryRepository;

    @Override
    public GetUserSelfInfoResult execute(GetUserSelfInfoCommand command) {
        return GetUserSelfInfoResult.from(userQueryRepository.getUserSelfInfoSummary(command.user().getId())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)));
    }

}
