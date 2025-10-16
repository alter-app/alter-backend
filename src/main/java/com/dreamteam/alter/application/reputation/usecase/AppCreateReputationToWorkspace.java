package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.CreateReputationToWorkspaceRequestDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.ReputationKeywordMapDto;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.reputation.entity.ReputationKeyword;
import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.port.inbound.AppCreateReputationToWorkspaceUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationKeywordQueryRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestType;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Service("appCreateReputationToWorkspace")
@Transactional
public class AppCreateReputationToWorkspace extends AbstractCreateReputation implements
                                                                             AppCreateReputationToWorkspaceUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;

    public AppCreateReputationToWorkspace(
        ReputationRequestRepository reputationRequestRepository,
        ReputationRepository reputationRepository,
        ReputationKeywordQueryRepository reputationKeywordQueryRepository,
        NotificationService notificationService,
        WorkspaceQueryRepository workspaceQueryRepository,
        WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository
    ) {
        super(reputationRequestRepository, reputationRepository, reputationKeywordQueryRepository, notificationService);
        this.workspaceQueryRepository = workspaceQueryRepository;
        this.workspaceWorkerQueryRepository = workspaceWorkerQueryRepository;
    }

    @Override
    public void execute(AppActor actor, CreateReputationToWorkspaceRequestDto request) {
        Set<ReputationKeywordMapDto> keywords = request.getKeywords();
        Map<String, ReputationKeyword> keywordMap = validateAndGetKeywords(keywords);

        Workspace targetWorkspace = workspaceQueryRepository.findById(request.getWorkspaceId())
            .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

        // TODO: 근무를 종료한 사용자도 요청이 가능해야함
        if (workspaceWorkerQueryRepository.findActiveWorkerByWorkspaceAndUser(targetWorkspace, actor.getUser()).isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND, "해당 업장에서 근무중인 사용자가 아닙니다.");
        }

        ReputationRequest reputationRequest = reputationRequestRepository.save(
            ReputationRequest.create(
                targetWorkspace,
                ReputationType.USER,
                actor.getUserId(),
                ReputationRequestType.USER_TO_WORKSPACE,
                ReputationType.WORKSPACE,
                targetWorkspace.getId()
            )
        );

        createReputation(
            reputationRequest,
            ReputationType.USER,
            actor.getUserId(),
            ReputationType.WORKSPACE,
            targetWorkspace.getId(),
            null,
            keywords,
            keywordMap
        );

        // 업장 관리자에게 알림 발송
        String title = "새로운 평판 요청";
        String body = String.format("%s님이 %s으로 평판을 요청했습니다.", 
            actor.getUser().getNickname(), 
            targetWorkspace.getBusinessName());

        sendNotificationToTarget(targetWorkspace.getManagerUser().getUser().getId(), title, body);
    }

}
