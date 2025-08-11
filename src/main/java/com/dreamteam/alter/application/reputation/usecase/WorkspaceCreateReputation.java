package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.WorkspaceCreateReputationRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.reputation.entity.Reputation;
import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.reputation.port.inbound.WorkspaceCreateReputationUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestType;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("workspaceCreateReputation")
@RequiredArgsConstructor
@Transactional
public class WorkspaceCreateReputation implements WorkspaceCreateReputationUseCase {

    private final ReputationRequestRepository reputationRequestRepository;
    private final UserQueryRepository userQueryRepository;
    private final ReputationRepository reputationRepository;

    @Override
    public void execute(ManagerActor actor, WorkspaceCreateReputationRequestDto request) {
        User targetUser = userQueryRepository.findById(request.getTargetUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Workspace workspace = actor.getManagerUser()
            .getWorkspaces().stream()
            .filter(it -> it.getId()
                .equals(request.getWorkspaceId()))
            .findFirst()
            .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

        ReputationRequest reputationRequest = reputationRequestRepository.save(
            ReputationRequest.create(
                workspace,
                targetUser.getId(),
                ReputationRequestType.WORKSPACE_TO_USER,
                targetUser.getId()
            )
        );

        reputationRepository.save(
            Reputation.create(
                reputationRequest,
                ReputationType.WORKSPACE,
                workspace.getId(),
                ReputationType.USER,
                targetUser.getId(),
                workspace.getId()
            )
        );
    }
}
