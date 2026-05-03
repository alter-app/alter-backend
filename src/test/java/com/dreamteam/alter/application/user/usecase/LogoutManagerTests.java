package com.dreamteam.alter.application.user.usecase;

import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("LogoutManager 테스트")
class LogoutManagerTests {

    @Mock private AuthService authService;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private LogoutManager logoutManager;

    private ManagerActor actor;
    private User user;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        given(user.getId()).willReturn(1L);

        ManagerUser managerUser = mock(ManagerUser.class);
        given(managerUser.getUser()).willReturn(user);

        actor = mock(ManagerActor.class);
        given(actor.getManagerUser()).willReturn(managerUser);
    }

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("execute() → authService.revokeAllExistingAuthorizations() 호출")
        void calls_revokeAllExistingAuthorizations() {
            // when
            logoutManager.execute(actor);

            // then
            then(authService).should().revokeAllExistingAuthorizations(user);
        }

        @Test
        @DisplayName("execute() → notificationService.removeUserDeviceToken(user) 호출")
        void calls_removeUserDeviceToken() {
            // when
            logoutManager.execute(actor);

            // then
            then(notificationService).should().removeUserDeviceToken(user);
        }
    }
}
