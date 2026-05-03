package com.dreamteam.alter.application.notification.usecase;

import com.dreamteam.alter.domain.notification.command.GetNotificationConsentCommand;
import com.dreamteam.alter.domain.notification.entity.NotificationConsent;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationConsentQueryRepository;
import com.dreamteam.alter.domain.notification.result.GetNotificationConsentResult;
import com.dreamteam.alter.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("GetNotificationConsent 테스트")
class GetNotificationConsentTests {

    @Mock
    private NotificationConsentQueryRepository notificationConsentQueryRepository;

    @InjectMocks
    private GetNotificationConsent getNotificationConsent;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("레코드가 없을 때 기본값(notificationConsent=true, nightNotificationConsent=true) 반환")
        void returnsDefaults_whenNoRecordExists() {
            // given
            User user = mock(User.class);
            GetNotificationConsentCommand command = GetNotificationConsentCommand.from(user);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.empty());

            // when
            GetNotificationConsentResult result = getNotificationConsent.execute(command);

            // then
            assertThat(result.notificationConsent()).isTrue();
            assertThat(result.nightNotificationConsent()).isTrue();
        }

        @Test
        @DisplayName("레코드 존재 + 둘 다 true이면 그대로 반환")
        void returnsActualValues_whenBothTrue() {
            // given
            User user = mock(User.class);
            GetNotificationConsentCommand command = GetNotificationConsentCommand.from(user);

            NotificationConsent consent = NotificationConsent.createDefault(user);
            // createDefault already sets both to true
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(consent));

            // when
            GetNotificationConsentResult result = getNotificationConsent.execute(command);

            // then
            assertThat(result.notificationConsent()).isTrue();
            assertThat(result.nightNotificationConsent()).isTrue();
        }

        @Test
        @DisplayName("레코드 존재 + notificationConsent=false이면 false 반환")
        void returnsActualValues_whenNotificationConsentIsFalse() {
            // given
            User user = mock(User.class);
            GetNotificationConsentCommand command = GetNotificationConsentCommand.from(user);

            NotificationConsent consent = NotificationConsent.createDefault(user);
            ReflectionTestUtils.setField(consent, "notificationConsent", false);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(consent));

            // when
            GetNotificationConsentResult result = getNotificationConsent.execute(command);

            // then
            assertThat(result.notificationConsent()).isFalse();
            assertThat(result.nightNotificationConsent()).isTrue();
        }

        @Test
        @DisplayName("레코드 존재 + nightNotificationConsent=false이면 false 반환")
        void returnsActualValues_whenNightNotificationConsentIsFalse() {
            // given
            User user = mock(User.class);
            GetNotificationConsentCommand command = GetNotificationConsentCommand.from(user);

            NotificationConsent consent = NotificationConsent.createDefault(user);
            ReflectionTestUtils.setField(consent, "nightNotificationConsent", false);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(consent));

            // when
            GetNotificationConsentResult result = getNotificationConsent.execute(command);

            // then
            assertThat(result.notificationConsent()).isTrue();
            assertThat(result.nightNotificationConsent()).isFalse();
        }
    }
}
