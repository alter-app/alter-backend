package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.port.inbound.WorkspaceAcceptReputationRequestUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationKeywordQueryRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestQueryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestStatus;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

@Service("workspaceAcceptReputationRequest")
public class WorkspaceAcceptReputationRequest extends AbstractAcceptReputationRequest<ManagerActor> implements
                                                                                                    WorkspaceAcceptReputationRequestUseCase {

    private final ReputationRequestQueryRepository reputationRequestQueryRepository;
    private final WorkspaceQueryRepository workspaceQueryRepository;

    public WorkspaceAcceptReputationRequest(
        ReputationRepository reputationRepository,
        ReputationKeywordQueryRepository reputationKeywordQueryRepository,
        ReputationRequestQueryRepository reputationRequestQueryRepository,
        WorkspaceQueryRepository workspaceQueryRepository
    ) {
        super(reputationRepository, reputationKeywordQueryRepository);
        this.reputationRequestQueryRepository = reputationRequestQueryRepository;
        this.workspaceQueryRepository = workspaceQueryRepository;
    }

    @Override
    protected ReputationRequest findAndValidateReputationRequest(ManagerActor actor, Long requestId) {
        ReputationRequest reputationRequest = reputationRequestQueryRepository.findById(requestId);

        if (ObjectUtils.isEmpty(reputationRequest)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "평판 요청을 찾을 수 없습니다.");
        }

        if (!ReputationType.WORKSPACE.equals(reputationRequest.getTargetType())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "평판 요청의 대상이 업장이 아닙니다.");
        }

        if (!ReputationRequestStatus.REQUESTED.equals(reputationRequest.getStatus())) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 처리된 평판 요청입니다.");
        }

        Workspace workspace = workspaceQueryRepository.findById(reputationRequest.getTargetId())
            .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

        if (!workspace.getManagerUser().equals(actor.getManagerUser())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "해당 업장에 대한 관리 권한이 없습니다.");
        }

        return reputationRequest;
    }

    @Override
    protected ReputationInfo getReputationInfo(ReputationRequest reputationRequest, ManagerActor actor) {
        Workspace workspace = workspaceQueryRepository.findById(reputationRequest.getTargetId())
            .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

        return new ReputationInfo(
            ReputationType.WORKSPACE,
            workspace.getId(),
            reputationRequest.getWorkspace()
        );
    }
}
