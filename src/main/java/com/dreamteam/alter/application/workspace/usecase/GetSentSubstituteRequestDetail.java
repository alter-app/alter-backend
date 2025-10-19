package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.SubstituteRequestDetailResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.inbound.GetSentSubstituteRequestDetailUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.SubstituteRequestQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("getSentSubstituteRequestDetail")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetSentSubstituteRequestDetail implements GetSentSubstituteRequestDetailUseCase {

    private final SubstituteRequestQueryRepository substituteRequestQueryRepository;

    @Override
    public SubstituteRequestDetailResponseDto execute(AppActor actor, Long requestId) {
        return substituteRequestQueryRepository.getSentRequestDetail(actor.getUser(), requestId)
            .map(SubstituteRequestDetailResponseDto::of)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 대타 요청입니다."));
    }
}
