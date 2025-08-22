package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.ReputationKeywordMapDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.CreateReputationToUserRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.reputation.entity.ReputationKeyword;
import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationKeywordQueryRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.reputation.port.inbound.AppCreateReputationToUserUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestType;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Service("userCreateReputation")
@Transactional
public class AppCreateReputationToUser extends AbstractCreateReputation implements AppCreateReputationToUserUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final UserQueryRepository userQueryRepository;
    private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;

    public AppCreateReputationToUser(
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
    public void execute(AppActor actor, CreateReputationToUserRequestDto request) {
        Set<ReputationKeywordMapDto> keywords = request.getKeywords();
        Map<String, ReputationKeyword> keywordMap = validateAndGetKeywords(keywords);

        ReputationRequestType requestType = request.getRequestType();
        Workspace workspace = null;

        if (!ReputationRequestType.toUserAvailableTypes().contains(request.getRequestType())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "유효하지 않은 요청 타입입니다.");
        }

        User targetUser = userQueryRepository.findById(request.getTargetUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (ReputationRequestType.USER_TO_USER_INTERNAL.equals(requestType)) {
            if (ObjectUtils.isEmpty(request.getWorkspaceId())) {
                throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "workspaceId가 비어있습니다.");
            }

            workspace = workspaceQueryRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

            // TODO: 근무를 종료한 사용자에게도 요청이 가능해야함
            if (ObjectUtils.isEmpty(
                workspaceWorkerQueryRepository.findActiveWorkerByWorkspaceAndUser(workspace, targetUser))
            ) {
                throw new CustomException(ErrorCode.NOT_FOUND, "해당 업장에서 근무중인 사용자가 아닙니다.");
            }
        }

        ReputationRequest reputationRequest = reputationRequestRepository.save(
            ReputationRequest.create(
                requestType.equals(ReputationRequestType.USER_TO_USER_EXTERNAL) ? null : workspace,
                ReputationType.USER,
                actor.getUserId(),
                requestType,
                ReputationType.USER,
                targetUser.getId()
            )
        );

        createReputation(
            reputationRequest,
            ReputationType.USER,
            actor.getUserId(),
            ReputationType.USER,
            targetUser.getId(),
            ReputationRequestType.USER_TO_USER_INTERNAL.equals(requestType) ? workspace : null,
            keywords,
            keywordMap
        );
    }
}
