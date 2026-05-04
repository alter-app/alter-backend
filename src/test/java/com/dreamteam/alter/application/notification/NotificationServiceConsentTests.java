package com.dreamteam.alter.application.notification;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmBatchNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.UserFcmDeviceTokenQueryRepository;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.entity.NotificationConsent;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationConsentQueryRepository;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationRepository;
import com.dreamteam.alter.domain.user.entity.FcmDeviceToken;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserFcmDeviceTokenRepository;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NotificationService 수신 동의 필터링 테스트")
class NotificationServiceConsentTests {

    @Mock private FcmClient fcmClient;
    @Mock private UserFcmDeviceTokenRepository userFCMDeviceTokenRepository;
    @Mock private UserFcmDeviceTokenQueryRepository userFCMDeviceTokenQueryRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private UserQueryRepository userQueryRepository;
    @Mock private EntityManager entityManager;
    @Mock private NotificationConsentQueryRepository notificationConsentQueryRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private FcmDeviceToken deviceToken;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        given(user.getId()).willReturn(1L);

        deviceToken = mock(FcmDeviceToken.class);
        given(deviceToken.getDeviceToken()).willReturn("test-device-token");
        given(deviceToken.getUser()).willReturn(user);

        given(userFCMDeviceTokenQueryRepository.findByUser(user)).willReturn(Optional.of(deviceToken));
        given(userQueryRepository.findById(1L)).willReturn(Optional.of(user));
    }

    // ── NotificationConsent 헬퍼 ──────────────────────────────────────────
    private NotificationConsent consentWith(boolean notificationConsent, boolean nightNotificationConsent) {
        return NotificationConsent.create(user, notificationConsent, nightNotificationConsent);
    }

    // ── 고정 시각을 반환하는 ZonedDateTime mock 헬퍼 ──────────────────────
    private ZonedDateTime kstAt(int hour) {
        return ZonedDateTime.of(2024, 1, 1, hour, 0, 0, 0, ZoneId.of("Asia/Seoul"));
    }

    // ── FcmNotificationRequestDto 헬퍼 ───────────────────────────────────
    private FcmNotificationRequestDto notificationRequest(Long userId) {
        return FcmNotificationRequestDto.of(userId, TokenScope.APP, "제목", "내용");
    }

    // ── FcmBatchNotificationRequestDto 헬퍼 ──────────────────────────────
    private FcmBatchNotificationRequestDto batchRequest(List<Long> userIds) {
        return FcmBatchNotificationRequestDto.of(userIds, TokenScope.APP, "제목", "내용");
    }

    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("sendNotification — 수신 동의 검사")
    class SendNotificationConsentTests {

        @Test
        @DisplayName("수신 동의 false → FCM 미호출, Notification 레코드는 저장됨")
        void skips_fcm_when_notificationConsent_is_false() throws Exception {
            // given
            NotificationConsent consent = consentWith(false, true);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(consent));

            // when
            notificationService.sendNotification(notificationRequest(1L));

            // then
            then(fcmClient).should(never()).sendNotification(any(), any(), any());
            then(notificationRepository).should().save(any());
        }

        @Test
        @DisplayName("야간 동의 false + 야간 시간(22시 KST) → FCM 미호출, Notification 레코드 저장됨")
        void skips_fcm_when_nightConsent_false_and_is_night() throws Exception {
            // given
            NotificationConsent consent = consentWith(true, false);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(consent));

            ZonedDateTime nightTime = kstAt(22);
            try (MockedStatic<ZonedDateTime> mockedStatic = mockStatic(ZonedDateTime.class, CALLS_REAL_METHODS)) {
                mockedStatic.when(() -> ZonedDateTime.now(ZoneId.of("Asia/Seoul"))).thenReturn(nightTime);

                // when
                notificationService.sendNotification(notificationRequest(1L));
            }

            // then
            then(fcmClient).should(never()).sendNotification(any(), any(), any());
            then(notificationRepository).should().save(any());
        }

        @Test
        @DisplayName("야간 동의 false + 주간 시간(10시 KST) → FCM 호출됨")
        void sends_fcm_when_nightConsent_false_but_is_daytime() throws Exception {
            // given
            NotificationConsent consent = consentWith(true, false);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(consent));

            ZonedDateTime dayTime = kstAt(10);
            try (MockedStatic<ZonedDateTime> mockedStatic = mockStatic(ZonedDateTime.class, CALLS_REAL_METHODS)) {
                mockedStatic.when(() -> ZonedDateTime.now(ZoneId.of("Asia/Seoul"))).thenReturn(dayTime);

                // when
                notificationService.sendNotification(notificationRequest(1L));
            }

            // then
            then(fcmClient).should().sendNotification(eq("test-device-token"), any(), any());
        }

        @Test
        @DisplayName("수신 동의 레코드 없는 사용자(empty) → NOTIFICATION_CONSENT_NOT_FOUND 예외")
        void throws_exception_when_no_consent_record_exists() {
            // given
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> notificationService.sendNotification(notificationRequest(1L)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOTIFICATION_CONSENT_NOT_FOUND);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("sendMultipleNotifications — 배치 수신 동의 필터")
    class SendMultipleNotificationsConsentTests {

        @Test
        @DisplayName("미동의 사용자가 포함된 배치 → 미동의 사용자 제외하고 FCM 배치 발송, Notification 레코드는 전원 저장됨")
        void excludes_non_consenting_user_from_fcm_batch_but_saves_all_notifications() throws Exception {
            // given
            User consentingUser = mock(User.class);
            given(consentingUser.getId()).willReturn(10L);

            User nonConsentingUser = mock(User.class);
            given(nonConsentingUser.getId()).willReturn(20L);

            given(userQueryRepository.findAllById(List.of(10L, 20L)))
                .willReturn(List.of(consentingUser, nonConsentingUser));

            FcmDeviceToken consentingToken = mock(FcmDeviceToken.class);
            given(consentingToken.getDeviceToken()).willReturn("token-consenting");
            given(consentingToken.getUser()).willReturn(consentingUser);

            FcmDeviceToken nonConsentingToken = mock(FcmDeviceToken.class);
            given(nonConsentingToken.getDeviceToken()).willReturn("token-non-consenting");
            given(nonConsentingToken.getUser()).willReturn(nonConsentingUser);

            given(userFCMDeviceTokenQueryRepository.findByUsers(anyList()))
                .willReturn(List.of(consentingToken, nonConsentingToken));

            // findByUsers 배치 응답: consentingUser는 동의, nonConsentingUser는 미동의
            NotificationConsent consentingConsent = NotificationConsent.create(consentingUser, true, true);
            NotificationConsent nonConsentingConsent = NotificationConsent.create(nonConsentingUser, false, true);
            given(notificationConsentQueryRepository.findByUsers(anyList()))
                .willReturn(List.of(consentingConsent, nonConsentingConsent));

            com.google.firebase.messaging.BatchResponse batchResponse =
                mock(com.google.firebase.messaging.BatchResponse.class);
            com.google.firebase.messaging.SendResponse sendResponse =
                mock(com.google.firebase.messaging.SendResponse.class);
            given(sendResponse.isSuccessful()).willReturn(true);
            given(batchResponse.getResponses()).willReturn(List.of(sendResponse));
            given(fcmClient.sendMultipleNotifications(anyList(), any(), any())).willReturn(batchResponse);

            // when
            notificationService.sendMultipleNotifications(batchRequest(List.of(10L, 20L)));

            // then: Notification 레코드는 두 사용자 모두 저장
            then(notificationRepository).should().saveAll(argThat(list -> ((List<?>) list).size() == 2));

            // then: FCM 배치는 동의한 사용자의 토큰만 포함
            then(fcmClient).should().sendMultipleNotifications(
                argThat(tokens -> {
                    @SuppressWarnings("unchecked")
                    List<String> tokenList = (List<String>) tokens;
                    return tokenList.size() == 1 && tokenList.contains("token-consenting");
                }),
                any(), any()
            );
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("sendNotificationOnly — 수신 동의 검사")
    class SendNotificationOnlyConsentTests {

        @Test
        @DisplayName("수신 동의 false → FCM 미호출")
        void skips_fcm_when_notificationConsent_is_false() throws Exception {
            // given
            NotificationConsent consent = consentWith(false, true);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(consent));

            // when
            notificationService.sendNotificationOnly(1L, "제목", "내용");

            // then
            then(fcmClient).should(never()).sendNotification(any(), any(), any());
        }

        @Test
        @DisplayName("야간 동의 false + 야간 시간(22시 KST) → FCM 미호출")
        void skips_fcm_when_nightConsent_false_and_is_night() throws Exception {
            // given
            NotificationConsent consent = consentWith(true, false);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(consent));

            ZonedDateTime nightTime = kstAt(22);
            try (MockedStatic<ZonedDateTime> mockedStatic = mockStatic(ZonedDateTime.class, CALLS_REAL_METHODS)) {
                mockedStatic.when(() -> ZonedDateTime.now(ZoneId.of("Asia/Seoul"))).thenReturn(nightTime);

                // when
                notificationService.sendNotificationOnly(1L, "제목", "내용");
            }

            // then
            then(fcmClient).should(never()).sendNotification(any(), any(), any());
        }
    }
}
