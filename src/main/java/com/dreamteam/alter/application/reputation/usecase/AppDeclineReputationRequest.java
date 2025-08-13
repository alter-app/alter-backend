package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.port.inbound.AppDeclineReputationRequestUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestQueryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.user.context.AppActor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("appDeclineReputationRequest")
@RequiredArgsConstructor
@Transactional
public class AppDeclineReputationRequest implements AppDeclineReputationRequestUseCase {

    private final ReputationRequestQueryRepository reputationRequestQueryRepository;

    @Override
    public void execute(AppActor actor, Long requestId) {
        ReputationRequest reputationRequest =
            reputationRequestQueryRepository.findByTargetAndId(ReputationType.USER, actor.getUserId(), requestId);

        if (ObjectUtils.isEmpty(reputationRequest)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "평판 요청을 찾을 수 없습니다.");
        }

        reputationRequest.decline();
    }

}
