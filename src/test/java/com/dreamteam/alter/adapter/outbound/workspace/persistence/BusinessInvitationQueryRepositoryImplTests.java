package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.user.persistence.ManagerUserRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.user.persistence.UserRepositoryImpl;
import com.dreamteam.alter.common.config.QueryDslConfig;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.type.ManagerUserStatus;
import com.dreamteam.alter.domain.user.type.UserGender;
import com.dreamteam.alter.domain.workspace.entity.BusinessInvitation;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
    QueryDslConfig.class,
    BusinessInvitationQueryRepositoryImpl.class,
    BusinessInvitationRepositoryImpl.class,
    UserRepositoryImpl.class,
    ManagerUserRepositoryImpl.class,
    WorkspaceRepositoryImpl.class,
    BusinessTypeRepositoryImpl.class
})
class BusinessInvitationQueryRepositoryImplTests {

    @Autowired
    private BusinessInvitationQueryRepositoryImpl businessInvitationQueryRepository;

    @Autowired
    private BusinessInvitationRepositoryImpl businessInvitationRepository;

    @Autowired
    private UserRepositoryImpl userRepository;

    @Autowired
    private ManagerUserRepositoryImpl managerUserRepository;

    @Autowired
    private WorkspaceRepositoryImpl workspaceRepository;

    @Autowired
    private BusinessTypeRepositoryImpl businessTypeRepository;

    private User saveUser() {
        User user = User.create(
            "010" + String.valueOf(System.nanoTime()).substring(0, 8), "encoded", "김알바",
            "nickname" + System.nanoTime(), UserGender.GENDER_MALE, "19990101",
            "user" + System.nanoTime() + "@example.com"
        );
        return userRepository.save(user);
    }

    private ManagerUser saveManagerUser() {
        User user = saveUser();
        return managerUserRepository.save(ManagerUser.create(user, ManagerUserStatus.ACTIVATED));
    }

    private Workspace saveWorkspace(ManagerUser managerUser) {
        BusinessType businessType = businessTypeRepository.save(
            BusinessType.create("업종" + System.nanoTime(), null));
        Workspace workspace = Workspace.create(
            managerUser, "000-00-00000", "사장님가게", businessType, null,
            "01000000000", "설명", WorkspaceStatus.ACTIVATED, "서울시 강남구",
            "서울특별시", "강남구", "역삼동", BigDecimal.ONE, BigDecimal.ONE
        );
        workspaceRepository.save(workspace);
        return workspace;
    }

    private BusinessInvitation saveInvitation(Workspace workspace, User invitedUser, ManagerUser invitedBy) {
        BusinessInvitation invitation = BusinessInvitation.create(workspace, invitedUser, invitedBy);
        businessInvitationRepository.save(invitation);
        return invitation;
    }

    private void expire(BusinessInvitation invitation) {
        ReflectionTestUtils.setField(invitation, "expiresAt", LocalDateTime.now().minusDays(1));
        businessInvitationRepository.save(invitation);
    }

    @Test
    void findPendingInvitedUserIdsByUserIds_만료된_PENDING_초대는_제외() {
        ManagerUser managerUser = saveManagerUser();
        Workspace workspace = saveWorkspace(managerUser);
        User expiredUser = saveUser();
        User validUser = saveUser();

        BusinessInvitation expiredInvitation = saveInvitation(workspace, expiredUser, managerUser);
        expire(expiredInvitation);
        saveInvitation(workspace, validUser, managerUser);

        Set<Long> result = businessInvitationQueryRepository.findPendingInvitedUserIdsByUserIds(
            workspace.getId(), Set.of(expiredUser.getId(), validUser.getId()));

        assertThat(result).containsExactly(validUser.getId());
    }

    @Test
    void findByUserWithCursor_만료된_PENDING_초대는_목록에서_제외() {
        ManagerUser managerUser = saveManagerUser();
        Workspace workspace = saveWorkspace(managerUser);
        User invitedUser = saveUser();

        BusinessInvitation expiredPending = saveInvitation(workspace, invitedUser, managerUser);
        expire(expiredPending);
        BusinessInvitation validPending = saveInvitation(workspace, invitedUser, managerUser);
        BusinessInvitation declined = saveInvitation(workspace, invitedUser, managerUser);
        declined.decline();
        businessInvitationRepository.save(declined);

        CursorPageRequest<CursorDto> pageRequest = CursorPageRequest.of(null, 20);
        List<BusinessInvitation> result =
            businessInvitationQueryRepository.findByUserWithCursor(pageRequest, invitedUser, null);
        long count = businessInvitationQueryRepository.countByUser(invitedUser, null);

        assertThat(result).extracting(BusinessInvitation::getId)
            .containsExactlyInAnyOrder(validPending.getId(), declined.getId())
            .doesNotContain(expiredPending.getId());
        assertThat(count).isEqualTo(2);
    }
}
