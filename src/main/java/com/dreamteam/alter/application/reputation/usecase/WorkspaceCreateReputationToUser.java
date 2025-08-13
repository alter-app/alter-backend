package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.CreateReputationToUserRequestDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.ReputationKeywordMapDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.reputation.entity.ReputationKeyword;
import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationKeywordQueryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.reputation.port.inbound.WorkspaceCreateReputationToUserUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestType;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Service("workspaceCreateReputationToUser")
@Transactional
public class WorkspaceCreateReputationToUser extends AbstractCreateReputation implements
                                                                              WorkspaceCreateReputationToUserUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final UserQueryRepository userQueryRepository;
    private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;

    public WorkspaceCreateReputationToUser(
        ReputationRequestRepository reputationRequestRepository,
        ReputationRepository reputationRepository,
        ReputationKeywordQueryRepository reputationKeywordQueryRepository,
        WorkspaceQueryRepository workspaceQueryRepository,
        UserQueryRepository userQueryRepository,
        WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository
    ) {
        super(reputationRequestRepository, reputationRepository, reputationKeywordQueryRepository);
        this.workspaceQueryRepository = workspaceQueryRepository;
        this.userQueryRepository = userQueryRepository;
        this.workspaceWorkerQueryRepository = workspaceWorkerQueryRepository;
    }

    @Override
    public void execute(ManagerActor actor, CreateReputationToUserRequestDto request) {
        Set<ReputationKeywordMapDto> keywords = request.getKeywords();
        Map<String, ReputationKeyword> keywordMap = validateAndGetKeywords(keywords);

        ReputationRequestType type = request.getRequestType();
        if (!ReputationRequestType.WORKSPACE_TO_USER.equals(type)) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "유효하지 않은 요청 타입입니다.");

        }

        Workspace workspace = workspaceQueryRepository.findById(request.getWorkspaceId())
            .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

        User targetUser = userQueryRepository.findById(request.getTargetUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // TODO: 근무를 종료한 사용자에게도 요청이 가능해야함
        if (workspaceWorkerQueryRepository.findActiveWorkerByWorkspaceAndUser(workspace, targetUser).isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND, "해당 업장에서 근무중인 사용자가 아닙니다.");
        }

        ReputationRequest reputationRequest = reputationRequestRepository.save(
            ReputationRequest.create(
                workspace,
                actor.getManagerId(),
                type,
                targetUser.getId()
            )
        );

        createReputation(
            reputationRequest,
            ReputationType.WORKSPACE,
            workspace.getId(),
            ReputationType.USER,
            targetUser.getId(),
            null,
            keywords,
            keywordMap
        );
    }
}
