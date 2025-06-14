package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.port.inbound.ExpireReputationRequestsUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestQueryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service("expireReputationRequests")
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ExpireReputationRequests implements ExpireReputationRequestsUseCase {

    private final ReputationRequestQueryRepository reputationRequestQueryRepository;

    @Override
    public void execute() {
        List<ReputationRequest> requestsToExpire = reputationRequestQueryRepository.findAllByStatusAndExpiredAtBefore(
            ReputationRequestStatus.REQUESTED, LocalDateTime.now()
        );

        if (ObjectUtils.isNotEmpty(requestsToExpire)) {
            requestsToExpire.forEach(ReputationRequest::expire);
        }

        log.info("ExpireReputationRequests: {} ReputationRequests have been expired.", requestsToExpire.size());
    }

}
