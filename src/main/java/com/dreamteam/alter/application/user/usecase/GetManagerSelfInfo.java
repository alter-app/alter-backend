package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.adapter.inbound.manager.user.dto.ManagerSelfInfoResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.port.inbound.GetManagerSelfInfoUseCase;
import com.dreamteam.alter.domain.user.port.outbound.ManagerUserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("getManagerSelfInfo")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetManagerSelfInfo implements GetManagerSelfInfoUseCase {

    private final ManagerUserQueryRepository managerUserQueryRepository;

    @Override
    public ManagerSelfInfoResponseDto execute(ManagerActor actor) {
        return ManagerSelfInfoResponseDto.from(managerUserQueryRepository.getManagerSelfInfoSummary(actor.getManagerId())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)));
    }
}
