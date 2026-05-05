package com.dreamteam.alter.application.notification.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.notification.command.UpdateNotificationConsentCommand;
import com.dreamteam.alter.domain.notification.entity.NotificationConsent;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationConsentQueryRepository;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationConsentRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
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
        @DisplayName("레코드가 없을 때 NOT_FOUND 예외를 던지고 save는 호출되지 않음")
        void throwsNotFound_whenNoRecordExists() {
            // given
            User user = mock(User.class);
            UpdateNotificationConsentCommand command =
                UpdateNotificationConsentCommand.of(user, NotificationConsentType.GENERAL, true);

            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> updateNotificationConsent.execute(command))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);

            then(notificationConsentRepository).should(never()).save(any(NotificationConsent.class));
        }

        @Test
        @DisplayName("GENERAL=true 토글 시 notificationConsent만 변경되어 저장됨")
        void savesGeneralTrue_keepsNightAsIs() {
            // given
            User user = mock(User.class);
            UpdateNotificationConsentCommand command =
                UpdateNotificationConsentCommand.of(user, NotificationConsentType.GENERAL, true);

            NotificationConsent existing = NotificationConsent.create(user, false, false);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(existing));

            // when
            updateNotificationConsent.execute(command);

            // then
            assertThat(existing.isNotificationConsent()).isTrue();
            assertThat(existing.isNightNotificationConsent()).isFalse();
            then(notificationConsentRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("GENERAL=false 토글 시 NIGHT도 자동으로 false로 cascade")
        void savesGeneralFalse_cascadesNightToFalse() {
            // given
            User user = mock(User.class);
            UpdateNotificationConsentCommand command =
                UpdateNotificationConsentCommand.of(user, NotificationConsentType.GENERAL, false);

            NotificationConsent existing = NotificationConsent.create(user, true, true);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(existing));

            // when
            updateNotificationConsent.execute(command);

            // then
            assertThat(existing.isNotificationConsent()).isFalse();
            assertThat(existing.isNightNotificationConsent()).isFalse();
            then(notificationConsentRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("GENERAL=true 상태에서 NIGHT=true 토글 시 정상 저장")
        void savesNightTrue_whenGeneralIsTrue() {
            // given
            User user = mock(User.class);
            UpdateNotificationConsentCommand command =
                UpdateNotificationConsentCommand.of(user, NotificationConsentType.NIGHT, true);

            NotificationConsent existing = NotificationConsent.create(user, true, false);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(existing));

            // when
            updateNotificationConsent.execute(command);

            // then
            assertThat(existing.isNotificationConsent()).isTrue();
            assertThat(existing.isNightNotificationConsent()).isTrue();
            then(notificationConsentRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("GENERAL=false 상태에서 NIGHT=true 토글 시 ILLEGAL_ARGUMENT 예외, save 미호출")
        void throwsIllegalArgument_whenNightTrueWithGeneralFalse() {
            // given
            User user = mock(User.class);
            UpdateNotificationConsentCommand command =
                UpdateNotificationConsentCommand.of(user, NotificationConsentType.NIGHT, true);

            NotificationConsent existing = NotificationConsent.create(user, false, false);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(existing));

            // when & then
            assertThatThrownBy(() -> updateNotificationConsent.execute(command))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ILLEGAL_ARGUMENT);

            then(notificationConsentRepository).should(never()).save(any(NotificationConsent.class));
        }

        @Test
        @DisplayName("NIGHT=false 토글 시 GENERAL은 유지하고 NIGHT만 false로 저장")
        void savesNightFalse_keepsGeneralAsIs() {
            // given
            User user = mock(User.class);
            UpdateNotificationConsentCommand command =
                UpdateNotificationConsentCommand.of(user, NotificationConsentType.NIGHT, false);

            NotificationConsent existing = NotificationConsent.create(user, true, true);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(existing));

            // when
            updateNotificationConsent.execute(command);

            // then
            assertThat(existing.isNotificationConsent()).isTrue();
            assertThat(existing.isNightNotificationConsent()).isFalse();
            then(notificationConsentRepository).shouldHaveNoInteractions();
        }
    }
}
