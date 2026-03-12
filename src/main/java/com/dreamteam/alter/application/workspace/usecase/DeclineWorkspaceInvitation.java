package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;
import com.dreamteam.alter.domain.workspace.port.inbound.DeclineWorkspaceInvitationUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessInvitationQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("declineWorkspaceInvitation")
@RequiredArgsConstructor
@Transactional
public class DeclineWorkspaceInvitation implements DeclineWorkspaceInvitationUseCase {

    private final BusinessInvitationQueryRepository businessInvitationQueryRepository;

    @Override
    public void execute(AppActor actor, Long invitationId) {
        BusinessInvitation invitation = businessInvitationQueryRepository.findById(invitationId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 초대입니다."));

        if (!invitation.getInvitedUser().getId().equals(actor.getUserId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "해당 초대의 수신자가 아닙니다.");
        }

        invitation.decline();
    }
}
