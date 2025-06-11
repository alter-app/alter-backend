package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.CreateReputationRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.reputation.entity.ReputationRequest;
import com.dreamteam.alter.domain.reputation.port.inbound.CreateReputationRequestUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationRequestRepository;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestType;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.type.UserStatus;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

@Service("createReputationRequest")
@RequiredArgsConstructor
@Transactional
public class CreateReputationRequest implements CreateReputationRequestUseCase {

    private final ReputationRequestRepository reputationRequestRepository;
    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final UserQueryRepository userQueryRepository;

    @Override
    public void execute(AppActor actor, CreateReputationRequestDto request) {
        ReputationRequestType type = request.getRequestType();
        Workspace workspace = null;
        User targetUser = null;

        switch (type) {
            case USER_TO_WORKSPACE:
                if (ObjectUtils.isEmpty(request.getWorkspaceId())) {
                    throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND); // TODO: fix
                }
                workspace = workspaceQueryRepository.findById(request.getWorkspaceId())
                    .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));
                break;
            case USER_TO_USER_INTERNAL:
            case WORKSPACE_TO_USER:
                if (ObjectUtils.isEmpty(request.getWorkspaceId())) {
                    throw new CustomException(ErrorCode.WORKSPACE_NOT_FOUND); // TODO: fix
                }
            case USER_TO_USER_EXTERNAL:
                targetUser = userQueryRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                if (!targetUser.getStatus().equals(UserStatus.ACTIVE)) {
                    throw new CustomException(ErrorCode.USER_NOT_FOUND);
                }

                // USER_TO_USER_EXTERNAL은 workspace 정보 불필요
                if (!type.equals(ReputationRequestType.USER_TO_USER_EXTERNAL)) {
                    workspace = workspaceQueryRepository.findById(request.getWorkspaceId())
                        .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));
                }
                break;
            default:
                throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        reputationRequestRepository.save(
            ReputationRequest.create(
                type.equals(ReputationRequestType.USER_TO_USER_EXTERNAL) ? null : workspace,
                actor.getUserId(),
                request
            )
        );
    }

}
