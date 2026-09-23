package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.SendWorkspaceInvitationRequestDto;
import com.dreamteam.alter.application.notification.FcmNotificationEvent;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.common.notification.NotificationMessageConstants;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.type.NotificationType;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.exception.InvitationUnavailableDetail;
import com.dreamteam.alter.domain.workspace.exception.InvitationUnavailableException;
import com.dreamteam.alter.domain.workspace.port.inbound.SendWorkspaceInvitationUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessInvitationQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessInvitationRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.type.InvitationUnavailableReason;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.type.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    public void execute(ManagerActor actor, Long workspaceId, SendWorkspaceInvitationRequestDto request) {
        LocalDateTime now = LocalDateTime.now();
        Workspace workspace = workspaceQueryRepository.findById(workspaceId)
            .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

        if (!workspaceQueryRepository.existsByIdAndManagerUser(workspaceId, actor.getManagerUser())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "해당 업장의 관리자가 아닙니다.");
        }

        Set<String> phoneNumbers = request.getPhoneNumbers();
        Map<String, User> contactToUser = userQueryRepository.findByContactIn(phoneNumbers)
            .stream().collect(Collectors.toMap(User::getContact, Function.identity()));

        Set<Long> registeredUserIds = contactToUser.values().stream()
            .filter(user -> user.getStatus() == UserStatus.ACTIVE)
            .map(User::getId)
            .collect(Collectors.toSet());

        Set<Long> activeWorkerUserIds = workspaceQueryRepository.findActiveWorkerUserIdsByUserIds(workspaceId, registeredUserIds);
        Set<Long> pendingInvitedUserIds = businessInvitationQueryRepository.findPendingInvitedUserIdsByUserIds(workspaceId, registeredUserIds, now);

        List<InvitationUnavailableDetail> details = new ArrayList<>();
        List<BusinessInvitation> invitationsToSave = new ArrayList<>();

        for (String phoneNumber : phoneNumbers) {
            User invitedUser = contactToUser.get(phoneNumber);

            if (invitedUser == null) {
                details.add(new InvitationUnavailableDetail(phoneNumber, InvitationUnavailableReason.NOT_REGISTERED));
                continue;
            }
            if (invitedUser.getStatus() != UserStatus.ACTIVE) {
                details.add(new InvitationUnavailableDetail(phoneNumber, InvitationUnavailableReason.ACCOUNT_UNAVAILABLE));
                continue;
            }
            if (activeWorkerUserIds.contains(invitedUser.getId())) {
                details.add(new InvitationUnavailableDetail(phoneNumber, InvitationUnavailableReason.ALREADY_WORKING));
                continue;
            }
            if (pendingInvitedUserIds.contains(invitedUser.getId())) {
                details.add(new InvitationUnavailableDetail(phoneNumber, InvitationUnavailableReason.ALREADY_INVITED));
                continue;
            }

            invitationsToSave.add(BusinessInvitation.create(workspace, invitedUser, actor.getManagerUser()));
        }

        if (!details.isEmpty()) {
            throw new InvitationUnavailableException(details);
        }

        expireStalePendingInvitations(workspaceId, invitationsToSave, now);

        try {
            businessInvitationRepository.saveAll(invitationsToSave);
        } catch (DataIntegrityViolationException e) {
            // ponytail: 부분 유니크 인덱스 충돌은 같은 요청의 동시 제출이 대부분이라 저장 대상 전부를 ALREADY_INVITED 로 본다.
            // 번호별 정확한 판정이 필요하면 저장을 REQUIRES_NEW 로 분리하고 충돌 후 PENDING 을 재조회한다.
            throw new InvitationUnavailableException(invitationsToSave.stream()
                .map(inv -> new InvitationUnavailableDetail(inv.getInvitedUser().getContact(), InvitationUnavailableReason.ALREADY_INVITED))
                .toList());
        }
        invitationsToSave.forEach(inv -> sendInvitationNotification(workspace, inv.getInvitedUser()));
    }

    // 만료된 PENDING 은 부분 유니크 인덱스(status = PENDING)에 걸리므로 새 초대를 insert 하기 전에 EXPIRED 로 바꿔 flush 한다.
    private void expireStalePendingInvitations(Long workspaceId, List<BusinessInvitation> invitationsToSave, LocalDateTime now) {
        Set<Long> userIds = invitationsToSave.stream()
            .map(inv -> inv.getInvitedUser().getId())
            .collect(Collectors.toSet());
        List<BusinessInvitation> staleInvitations =
            businessInvitationQueryRepository.findExpiredPendingByWorkspaceAndUserIds(workspaceId, userIds, now);
        if (staleInvitations.isEmpty()) {
            return;
        }
        staleInvitations.forEach(BusinessInvitation::expire);
        businessInvitationRepository.saveAll(staleInvitations);
    }

    private void sendInvitationNotification(Workspace workspace, User invitedUser) {
        String title = NotificationMessageConstants.WorkspaceInvitation.INVITATION_RECEIVED_TITLE;
        String body = String.format(
            NotificationMessageConstants.WorkspaceInvitation.INVITATION_RECEIVED_BODY,
            workspace.getBusinessName()
        );
        eventPublisher.publishEvent(
            new FcmNotificationEvent(FcmNotificationRequestDto.of(invitedUser.getId(), TokenScope.APP, NotificationType.WORKSPACE_INVITATION, title, body))
        );
    }
}
