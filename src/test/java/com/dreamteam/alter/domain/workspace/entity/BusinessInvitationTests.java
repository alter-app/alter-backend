package com.dreamteam.alter.domain.workspace.entity;

import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.type.BusinessInvitationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BusinessInvitationTests {

    private BusinessInvitation pendingInvitation() {
        return BusinessInvitation.create(mock(Workspace.class), mock(User.class), mock(ManagerUser.class));
    }

    @Test
    void getEffectiveStatus_PENDING이고_만료됐으면_EXPIRED() {
        LocalDateTime now = LocalDateTime.now();
        BusinessInvitation invitation = pendingInvitation();
        ReflectionTestUtils.setField(invitation, "expiresAt", now.minusSeconds(1));

        assertThat(invitation.getEffectiveStatus(now)).isEqualTo(BusinessInvitationStatus.EXPIRED);
    }

    @Test
    void getEffectiveStatus_PENDING이고_유효하면_PENDING() {
        LocalDateTime now = LocalDateTime.now();
        BusinessInvitation invitation = pendingInvitation();
        ReflectionTestUtils.setField(invitation, "expiresAt", now.plusSeconds(1));

        assertThat(invitation.getEffectiveStatus(now)).isEqualTo(BusinessInvitationStatus.PENDING);
    }

    @Test
    void getEffectiveStatus_expiresAt이_now와_같으면_EXPIRED() {
        LocalDateTime now = LocalDateTime.now();
        BusinessInvitation invitation = pendingInvitation();
        ReflectionTestUtils.setField(invitation, "expiresAt", now);

        assertThat(invitation.getEffectiveStatus(now)).isEqualTo(BusinessInvitationStatus.EXPIRED);
    }
}
