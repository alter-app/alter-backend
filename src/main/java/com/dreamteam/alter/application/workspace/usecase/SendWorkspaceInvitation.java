package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.SendWorkspaceInvitationRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.SendWorkspaceInvitationResultDto;
import com.dreamteam.alter.application.notification.FcmNotificationEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service("sendWorkspaceInvitation")
@RequiredArgsConstructor
@Transactional
public class SendWorkspaceInvitation implements SendWorkspaceInvitationUseCase {

    private final WorkspaceQueryRepository workspaceQueryRepository;
    private final UserQueryRepository userQueryRepository;
    private final BusinessInvitationRepository businessInvitationRepository;
    private final BusinessInvitationQueryRepository businessInvitationQueryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public SendWorkspaceInvitationResultDto execute(ManagerActor actor, Long workspaceId, SendWorkspaceInvitationRequestDto request) {
        Workspace workspace = workspaceQueryRepository.findById(workspaceId)
            .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

        if (!workspaceQueryRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "해당 업장의 관리자가 아닙니다.");
        }

        List<String> phoneNumbers = request.getPhoneNumbers();
        Map<String, User> contactToUser = userQueryRepository.findByContactIn(phoneNumbers)
            .stream().collect(Collectors.toMap(User::getContact, Function.identity()));

        Set<Long> activeWorkerUserIds = workspaceQueryRepository.findActiveWorkerUserIds(workspaceId);
        Set<Long> pendingInvitedUserIds = businessInvitationQueryRepository.findPendingInvitedUserIds(workspaceId);

        List<String> unregisteredPhoneNumbers = new ArrayList<>();
        List<String> alreadyWorkerPhoneNumbers = new ArrayList<>();
        List<String> alreadyInvitedPhoneNumbers = new ArrayList<>();
        List<BusinessInvitation> invitationsToSave = new ArrayList<>();

        for (String phoneNumber : phoneNumbers) {
            User invitedUser = contactToUser.get(phoneNumber);

            if (invitedUser == null) {
                unregisteredPhoneNumbers.add(phoneNumber);
                continue;
            }
            if (activeWorkerUserIds.contains(invitedUser.getId())) {
                alreadyWorkerPhoneNumbers.add(phoneNumber);
                continue;
            }
            if (pendingInvitedUserIds.contains(invitedUser.getId())) {
                alreadyInvitedPhoneNumbers.add(phoneNumber);
                continue;
            }

            invitationsToSave.add(BusinessInvitation.create(workspace, invitedUser, actor.getManagerUser()));
        }

        businessInvitationRepository.saveAll(invitationsToSave);
        invitationsToSave.forEach(inv -> sendInvitationNotification(workspace, inv.getInvitedUser()));

        return SendWorkspaceInvitationResultDto.of(invitationsToSave.size(), unregisteredPhoneNumbers, alreadyWorkerPhoneNumbers, alreadyInvitedPhoneNumbers);
    }

    private void sendInvitationNotification(Workspace workspace, User invitedUser) {
        String title = NotificationMessageConstants.WorkspaceInvitation.INVITATION_RECEIVED_TITLE;
        String body = String.format(
            NotificationMessageConstants.WorkspaceInvitation.INVITATION_RECEIVED_BODY,
            workspace.getBusinessName()
        );
        eventPublisher.publishEvent(
            new FcmNotificationEvent(FcmNotificationRequestDto.of(invitedUser.getId(), TokenScope.APP, title, body))
        );
    }
}
