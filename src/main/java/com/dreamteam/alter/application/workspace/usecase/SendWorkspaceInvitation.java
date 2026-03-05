package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.SendWorkspaceInvitationResultDto;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.notification.NotificationMessageConstants;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.inbound.SendWorkspaceInvitationUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessInvitationQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessInvitationRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service("sendWorkspaceInvitation")
@RequiredArgsConstructor
@Transactional
public class SendWorkspaceInvitation implements SendWorkspaceInvitationUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final UserQueryRepository userQueryRepository;
    private final BusinessInvitationRepository businessInvitationRepository;
    private final BusinessInvitationQueryRepository businessInvitationQueryRepository;
    private final NotificationService notificationService;

    @Override
    public SendWorkspaceInvitationResultDto execute(ManagerActor actor, Long workspaceId, List<String> phoneNumbers) {
        Workspace workspace = workspaceQueryRepository.findById(workspaceId)
            .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

        if (!workspaceQueryRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "해당 업장의 관리자가 아닙니다.");
        }

        List<String> unregisteredPhoneNumbers = new ArrayList<>();
        List<String> alreadyWorkerPhoneNumbers = new ArrayList<>();
        List<String> alreadyInvitedPhoneNumbers = new ArrayList<>();
        int successCount = 0;

        for (String phoneNumber : phoneNumbers) {
            Optional<User> userOpt = userQueryRepository.findByContact(phoneNumber);

            if (userOpt.isEmpty()) {
                unregisteredPhoneNumbers.add(phoneNumber);
                continue;
            }

            User invitedUser = userOpt.get();

            if (workspaceQueryRepository.isUserActiveWorkerInWorkspace(invitedUser, workspaceId)) {
                alreadyWorkerPhoneNumbers.add(phoneNumber);
                continue;
            }
            if (businessInvitationQueryRepository.existsPendingInvitation(workspace, invitedUser)) {
                alreadyInvitedPhoneNumbers.add(phoneNumber);
                continue;
            }

            businessInvitationRepository.save(
                BusinessInvitation.create(workspace, invitedUser, actor.getManagerUser())
            );
            successCount++;

            sendInvitationNotification(workspace, invitedUser);
        }

        return new SendWorkspaceInvitationResultDto(successCount, unregisteredPhoneNumbers, alreadyWorkerPhoneNumbers, alreadyInvitedPhoneNumbers);
    }

    private void sendInvitationNotification(Workspace workspace, User invitedUser) {
        try {
            String title = NotificationMessageConstants.WorkspaceInvitation.INVITATION_RECEIVED_TITLE;
            String body = String.format(
                NotificationMessageConstants.WorkspaceInvitation.INVITATION_RECEIVED_BODY,
                workspace.getBusinessName()
            );
            notificationService.sendNotification(
                FcmNotificationRequestDto.of(invitedUser.getId(), TokenScope.APP, title, body)
            );
        } catch (Exception e) {
            log.warn("업장 초대 알림 발송 실패. userId={}, error={}", invitedUser.getId(), e.getMessage());
        }
    }
}
