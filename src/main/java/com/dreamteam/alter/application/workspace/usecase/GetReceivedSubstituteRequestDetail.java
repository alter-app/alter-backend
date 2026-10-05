package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.workspace.command.GetReceivedSubstituteRequestDetailCommand;
import com.dreamteam.alter.domain.workspace.port.inbound.GetReceivedSubstituteRequestDetailUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.SubstituteRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.result.ReceivedSubstituteRequestDetailResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("getReceivedSubstituteRequestDetail")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetReceivedSubstituteRequestDetail implements GetReceivedSubstituteRequestDetailUseCase {

    private final SubstituteRequestQueryRepository substituteRequestQueryRepository;

    @Override
    public ReceivedSubstituteRequestDetailResult execute(GetReceivedSubstituteRequestDetailCommand command) {
        return substituteRequestQueryRepository.getReceivedRequestDetail(command.user(), command.requestId())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 대타 요청입니다."));
    }
}
