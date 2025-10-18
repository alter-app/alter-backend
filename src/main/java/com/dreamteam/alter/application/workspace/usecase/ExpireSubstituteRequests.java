package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.domain.workspace.entity.SubstituteRequest;
import com.dreamteam.alter.domain.workspace.port.inbound.ExpireSubstituteRequestsUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.SubstituteRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service("expireSubstituteRequests")
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ExpireSubstituteRequests implements ExpireSubstituteRequestsUseCase {

    private final SubstituteRequestQueryRepository substituteRequestQueryRepository;

    @Override
    public void execute() {
        List<SubstituteRequest> requestsToExpire = substituteRequestQueryRepository.findAllByStatusAndExpiresAtBefore(
            SubstituteRequestStatus.PENDING, LocalDateTime.now()
        );

        if (ObjectUtils.isNotEmpty(requestsToExpire)) {
            requestsToExpire.forEach(SubstituteRequest::expire);
        }

        log.info("ExpireSubstituteRequests: {} SubstituteRequests have been expired.", requestsToExpire.size());
    }
}
