package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.port.inbound.ManagerCancelReputationRequestUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestQueryRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("managerCancelReputationRequest")
@RequiredArgsConstructor
@Transactional
public class ManagerCancelReputationRequest implements ManagerCancelReputationRequestUseCase {

    private final ReputationRequestQueryRepository reputationRequestQueryRepository;

    @Override
    public void execute(ManagerActor actor, Long requestId) {
        ReputationRequest reputationRequest = reputationRequestQueryRepository
            .findSentReputationRequestByManager(actor.getManagerUser().getId(), requestId);

        if (ObjectUtils.isEmpty(reputationRequest)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "취소할 수 있는 평판 요청을 찾을 수 없습니다.");
        }

        reputationRequest.cancel();
    }

}
