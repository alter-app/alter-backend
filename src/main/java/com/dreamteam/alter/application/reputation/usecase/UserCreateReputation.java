package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.UserCreateReputationRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.reputation.entity.Reputation;
import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import com.dreamteam.alter.domain.reputation.port.inbound.UserCreateReputationUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRepository;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestType;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("userCreateReputation")
@RequiredArgsConstructor
@Transactional
public class UserCreateReputation implements UserCreateReputationUseCase {

    private final ReputationRequestRepository reputationRequestRepository;
    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final UserQueryRepository userQueryRepository;
    private final ReputationRepository reputationRepository;

    @Override
    public void execute(AppActor actor, UserCreateReputationRequestDto request) {
        ReputationRequestType type = request.getRequestType();
        Workspace workspace = null;
        Long targetId = null;

        if (!ReputationRequestType.userAvailableTypes().contains(request.getRequestType())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        switch (type) {
            case USER_TO_WORKSPACE:
                if (ObjectUtils.isEmpty(request.getWorkspaceId())) {
                    throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "workspaceId가 비어있습니다.");
                }
                workspace = workspaceQueryRepository.findById(request.getWorkspaceId())
                    .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

                targetId = workspace.getId();
                break;
            case USER_TO_USER_INTERNAL:
                if (ObjectUtils.isEmpty(request.getWorkspaceId())) {
                    throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "workspaceId가 비어있습니다.");
                }
                workspace = workspaceQueryRepository.findById(request.getWorkspaceId())
                    .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));
                User targetUser = userQueryRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                targetId = targetUser.getId();
                break;
            case USER_TO_USER_EXTERNAL:
                User user = userQueryRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                targetId = user.getId();
                break;
            default:
                throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        ReputationRequest reputationRequest = reputationRequestRepository.save(
            ReputationRequest.create(
                type.equals(ReputationRequestType.USER_TO_USER_EXTERNAL) ? null : workspace,
                actor.getUserId(),
                type,
                targetId
            )
        );

        ReputationType targetType = ReputationRequestType.USER_TO_WORKSPACE.equals(type)
            ? ReputationType.WORKSPACE : ReputationType.USER;

        reputationRepository.save(
            Reputation.create(
                reputationRequest,
                ReputationType.USER,
                actor.getUserId(),
                targetType,
                targetId,
                request.getWorkspaceId()
            )
        );
    }
}
