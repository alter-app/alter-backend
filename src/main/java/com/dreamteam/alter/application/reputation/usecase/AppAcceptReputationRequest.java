package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.port.inbound.AppAcceptReputationRequestUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationKeywordQueryRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestQueryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.user.context.AppActor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

@Service("appAcceptReputationRequest")
public class AppAcceptReputationRequest extends AbstractAcceptReputationRequest<AppActor> implements
                                                                                          AppAcceptReputationRequestUseCase {

    private final ReputationRequestQueryRepository reputationRequestQueryRepository;

    public AppAcceptReputationRequest(
        ReputationRepository reputationRepository,
        ReputationKeywordQueryRepository reputationKeywordQueryRepository,
        ReputationRequestQueryRepository reputationRequestQueryRepository
    ) {
        super(reputationRepository, reputationKeywordQueryRepository);
        this.reputationRequestQueryRepository = reputationRequestQueryRepository;
    }

    @Override
    protected ReputationRequest findAndValidateReputationRequest(AppActor actor, Long requestId) {
        ReputationRequest reputationRequest = reputationRequestQueryRepository.findByTargetAndId(
            ReputationType.USER,
            actor.getUserId(),
            requestId
        );

        if (ObjectUtils.isEmpty(reputationRequest)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "평판 요청을 찾을 수 없습니다.");
        }

        return reputationRequest;
    }

    @Override
    protected ReputationInfo getReputationInfo(ReputationRequest reputationRequest, AppActor actor) {
        return new ReputationInfo(
            ReputationType.USER,
            actor.getUserId(),
            reputationRequest.getWorkspace()
        );
    }
}
