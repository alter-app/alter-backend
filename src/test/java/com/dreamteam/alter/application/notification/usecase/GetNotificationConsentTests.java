package com.dreamteam.alter.application.notification.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.notification.command.GetNotificationConsentCommand;
import com.dreamteam.alter.domain.notification.entity.NotificationConsent;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationConsentQueryRepository;
import com.dreamteam.alter.domain.notification.result.GetNotificationConsentResult;
import com.dreamteam.alter.domain.notification.type.NotificationConsentType;
import com.dreamteam.alter.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
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
        @DisplayName("레코드가 없을 때 NOT_FOUND 예외를 던짐")
        void throwsNotFound_whenNoRecordExists() {
            // given
            User user = mock(User.class);
            GetNotificationConsentCommand command = GetNotificationConsentCommand.from(user);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> getNotificationConsent.execute(command))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
        }

        @Test
        @DisplayName("레코드 존재 + 둘 다 true이면 GENERAL/NIGHT 모두 true 반환")
        void returnsActualValues_whenBothTrue() {
            // given
            User user = mock(User.class);
            GetNotificationConsentCommand command = GetNotificationConsentCommand.from(user);

            NotificationConsent consent = NotificationConsent.create(user, true, true);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(consent));

            // when
            GetNotificationConsentResult result = getNotificationConsent.execute(command);

            // then
            assertThat(result.consents().get(NotificationConsentType.GENERAL)).isTrue();
            assertThat(result.consents().get(NotificationConsentType.NIGHT)).isTrue();
        }

        @Test
        @DisplayName("레코드 존재 + GENERAL=false이면 NIGHT도 false 반환")
        void returnsActualValues_whenGeneralIsFalse() {
            // given
            User user = mock(User.class);
            GetNotificationConsentCommand command = GetNotificationConsentCommand.from(user);

            NotificationConsent consent = NotificationConsent.create(user, false, true);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(consent));

            // when
            GetNotificationConsentResult result = getNotificationConsent.execute(command);

            // then
            assertThat(result.consents().get(NotificationConsentType.GENERAL)).isFalse();
            assertThat(result.consents().get(NotificationConsentType.NIGHT)).isFalse();
        }

        @Test
        @DisplayName("레코드 존재 + NIGHT=false이면 false 반환")
        void returnsActualValues_whenNightIsFalse() {
            // given
            User user = mock(User.class);
            GetNotificationConsentCommand command = GetNotificationConsentCommand.from(user);

            NotificationConsent consent = NotificationConsent.create(user, true, false);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(consent));

            // when
            GetNotificationConsentResult result = getNotificationConsent.execute(command);

            // then
            assertThat(result.consents().get(NotificationConsentType.GENERAL)).isTrue();
            assertThat(result.consents().get(NotificationConsentType.NIGHT)).isFalse();
        }
    }
}
