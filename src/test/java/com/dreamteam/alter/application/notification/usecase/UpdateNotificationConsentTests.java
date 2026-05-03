package com.dreamteam.alter.application.notification.usecase;

import com.dreamteam.alter.domain.notification.command.UpdateNotificationConsentCommand;
import com.dreamteam.alter.domain.notification.entity.NotificationConsent;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationConsentQueryRepository;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationConsentRepository;
import com.dreamteam.alter.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UpdateNotificationConsent 테스트")
class UpdateNotificationConsentTests {

    @Mock
    private NotificationConsentRepository notificationConsentRepository;

    @Mock
    private NotificationConsentQueryRepository notificationConsentQueryRepository;

    @InjectMocks
    private UpdateNotificationConsent updateNotificationConsent;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("레코드가 없을 때 createDefault 후 updateConsent 호출 + save 호출됨")
        void createsDefaultAndSaves_whenNoRecordExists() {
            // given
            User user = mock(User.class);
            UpdateNotificationConsentCommand command =
                UpdateNotificationConsentCommand.of(user, true, false);

            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.empty());
            given(notificationConsentRepository.save(any(NotificationConsent.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // when
            updateNotificationConsent.execute(command);

            // then
            ArgumentCaptor<NotificationConsent> captor = ArgumentCaptor.forClass(NotificationConsent.class);
            then(notificationConsentRepository).should().save(captor.capture());

            NotificationConsent saved = captor.getValue();
            assertThat(saved.isNotificationConsent()).isTrue();
            assertThat(saved.isNightNotificationConsent()).isFalse();
        }

        @Test
        @DisplayName("기존 레코드 존재 시 updateConsent 호출 후 save 호출됨")
        void updatesExistingRecordAndSaves_whenRecordExists() {
            // given
            User user = mock(User.class);
            UpdateNotificationConsentCommand command =
                UpdateNotificationConsentCommand.of(user, true, true);

            NotificationConsent existing = NotificationConsent.createDefault(user);
            ReflectionTestUtils.setField(existing, "notificationConsent", false);
            ReflectionTestUtils.setField(existing, "nightNotificationConsent", false);

            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(existing));
            given(notificationConsentRepository.save(any(NotificationConsent.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // when
            updateNotificationConsent.execute(command);

            // then
            ArgumentCaptor<NotificationConsent> captor = ArgumentCaptor.forClass(NotificationConsent.class);
            then(notificationConsentRepository).should().save(captor.capture());

            NotificationConsent saved = captor.getValue();
            assertThat(saved.isNotificationConsent()).isTrue();
            assertThat(saved.isNightNotificationConsent()).isTrue();
        }

        @Test
        @DisplayName("nightNotificationConsent만 false로 변경 시 그 값으로 저장됨")
        void savesWithNightConsentFalse_whenOnlyNightConsentChanged() {
            // given
            User user = mock(User.class);
            UpdateNotificationConsentCommand command =
                UpdateNotificationConsentCommand.of(user, true, false);

            NotificationConsent existing = NotificationConsent.createDefault(user);
            // both flags currently true

            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(existing));
            given(notificationConsentRepository.save(any(NotificationConsent.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // when
            updateNotificationConsent.execute(command);

            // then
            ArgumentCaptor<NotificationConsent> captor = ArgumentCaptor.forClass(NotificationConsent.class);
            then(notificationConsentRepository).should().save(captor.capture());

            NotificationConsent saved = captor.getValue();
            assertThat(saved.isNotificationConsent()).isTrue();
            assertThat(saved.isNightNotificationConsent()).isFalse();
        }

        @Test
        @DisplayName("두 플래그 모두 false로 변경 시 그 값으로 저장됨")
        void savesWithBothFlagsFalse_whenBothChanged() {
            // given
            User user = mock(User.class);
            UpdateNotificationConsentCommand command =
                UpdateNotificationConsentCommand.of(user, false, false);

            NotificationConsent existing = NotificationConsent.createDefault(user);
            // both flags currently true

            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(existing));
            given(notificationConsentRepository.save(any(NotificationConsent.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // when
            updateNotificationConsent.execute(command);

            // then
            ArgumentCaptor<NotificationConsent> captor = ArgumentCaptor.forClass(NotificationConsent.class);
            then(notificationConsentRepository).should().save(captor.capture());

            NotificationConsent saved = captor.getValue();
            assertThat(saved.isNotificationConsent()).isFalse();
            assertThat(saved.isNightNotificationConsent()).isFalse();
        }
    }
}
