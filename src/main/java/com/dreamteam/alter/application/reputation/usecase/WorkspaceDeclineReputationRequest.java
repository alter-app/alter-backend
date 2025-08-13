package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.port.inbound.WorkspaceDeclineReputationRequestUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestQueryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("workspaceDeclineReputationRequest")
@RequiredArgsConstructor
@Transactional
public class WorkspaceDeclineReputationRequest implements WorkspaceDeclineReputationRequestUseCase {

    private final ReputationRequestQueryRepository reputationRequestQueryRepository;
    private final WorkspaceQueryRepository workspaceQueryRepository;

    @Override
    public void execute(ManagerActor actor, Long requestId) {
        ReputationRequest reputationRequest =
            reputationRequestQueryRepository.findById(requestId);

        if (ObjectUtils.isEmpty(reputationRequest)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "평판 요청을 찾을 수 없습니다.");
        }

        if (!ReputationType.WORKSPACE.equals(reputationRequest.getTargetType())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "평판 요청의 대상이 업장이 아닙니다.");
        }

        Workspace workspace = workspaceQueryRepository.findById(reputationRequest.getTargetId())
            .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

        if (!workspace.getManagerUser().equals(actor.getManagerUser())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "해당 업장에 대한 관리 권한이 없습니다.");
        }

        reputationRequest.decline();
    }

}
