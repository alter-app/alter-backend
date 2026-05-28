package com.dreamteam.alter.application.notification;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmBatchNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.UserFcmDeviceTokenQueryRepository;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.entity.NotificationConsent;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationConsentQueryRepository;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationRepository;
import com.dreamteam.alter.domain.notification.type.NotificationConsentType;
import com.dreamteam.alter.domain.notification.type.NotificationType;
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

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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

    private NotificationConsent consentWithAll(User targetUser, boolean general, boolean night, boolean substitute, boolean reputation) {
        NotificationConsent consent = NotificationConsent.create(targetUser, general, night);
        if (general && !substitute) {
            consent.updateConsent(NotificationConsentType.SUBSTITUTE, false);
        }
        if (general && !reputation) {
            consent.updateConsent(NotificationConsentType.REPUTATION, false);
        }
        return consent;
    }

    // ── 고정 시각을 반환하는 LocalTime 헬퍼 ──────────────────────────────
    private LocalTime localTimeAt(int hour) {
        return LocalTime.of(hour, 0);
    }

    // ── FcmNotificationRequestDto 헬퍼 ───────────────────────────────────
    private FcmNotificationRequestDto notificationRequest(Long userId) {
        return notificationRequest(userId, NotificationType.GENERAL);
    }

    private FcmNotificationRequestDto notificationRequest(Long userId, NotificationType type) {
        return FcmNotificationRequestDto.of(userId, TokenScope.APP, type, "제목", "내용");
    }

    // ── FcmBatchNotificationRequestDto 헬퍼 ──────────────────────────────
    private FcmBatchNotificationRequestDto batchRequest(List<Long> userIds) {
        return batchRequest(userIds, NotificationType.GENERAL);
    }

    private FcmBatchNotificationRequestDto batchRequest(List<Long> userIds, NotificationType type) {
        return FcmBatchNotificationRequestDto.of(userIds, TokenScope.APP, type, "제목", "내용");
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

            LocalTime nightTime = localTimeAt(22);
            try (MockedStatic<LocalTime> mockedStatic = mockStatic(LocalTime.class)) {
                mockedStatic.when(LocalTime::now).thenReturn(nightTime);

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

            LocalTime dayTime = localTimeAt(10);
            try (MockedStatic<LocalTime> mockedStatic = mockStatic(LocalTime.class)) {
                mockedStatic.when(LocalTime::now).thenReturn(dayTime);

                // when
                notificationService.sendNotification(notificationRequest(1L));
            }

            // then
            then(fcmClient).should().sendNotification(eq("test-device-token"), any(), any());
        }

        @Test
        @DisplayName("수신 동의 레코드 없는 사용자 → 미동의로 간주, FCM 미호출, 예외 미발생, Notification 레코드는 저장됨")
        void treats_missing_consent_as_blocked_and_skips_fcm_silently() throws Exception {
            // given
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.empty());

            // when
            notificationService.sendNotification(notificationRequest(1L));

            // then
            then(fcmClient).should(never()).sendNotification(any(), any(), any());
            then(notificationRepository).should().save(any());
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

        @Test
        @DisplayName("일부 사용자의 동의 레코드 누락 → 누락 사용자 제외하고 발송, 예외 미발생")
        void treats_missing_consent_as_blocked_in_batch_and_skips_user_silently() throws Exception {
            // given
            User userWithConsent = mock(User.class);
            given(userWithConsent.getId()).willReturn(10L);

            User userWithoutConsent = mock(User.class);
            given(userWithoutConsent.getId()).willReturn(20L);

            given(userQueryRepository.findAllById(List.of(10L, 20L)))
                .willReturn(List.of(userWithConsent, userWithoutConsent));

            FcmDeviceToken tokenWithConsent = mock(FcmDeviceToken.class);
            given(tokenWithConsent.getDeviceToken()).willReturn("token-with-consent");
            given(tokenWithConsent.getUser()).willReturn(userWithConsent);

            FcmDeviceToken tokenWithoutConsent = mock(FcmDeviceToken.class);
            given(tokenWithoutConsent.getDeviceToken()).willReturn("token-without-consent");
            given(tokenWithoutConsent.getUser()).willReturn(userWithoutConsent);

            given(userFCMDeviceTokenQueryRepository.findByUsers(anyList()))
                .willReturn(List.of(tokenWithConsent, tokenWithoutConsent));

            // userWithConsent만 동의 레코드 존재, userWithoutConsent는 누락
            NotificationConsent consent = NotificationConsent.create(userWithConsent, true, true);
            given(notificationConsentQueryRepository.findByUsers(anyList()))
                .willReturn(List.of(consent));

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

            // then: FCM 배치는 동의 레코드가 있는 사용자의 토큰만 포함
            then(fcmClient).should().sendMultipleNotifications(
                argThat(tokens -> {
                    @SuppressWarnings("unchecked")
                    List<String> tokenList = (List<String>) tokens;
                    return tokenList.size() == 1 && tokenList.contains("token-with-consent");
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
            notificationService.sendNotificationOnly(1L, NotificationType.CHAT, "제목", "내용");

            // then
            then(fcmClient).should(never()).sendNotification(any(), any(), any());
        }

        @Test
        @DisplayName("야간 동의 false + 야간 시간(22시 KST) → FCM 미호출")
        void skips_fcm_when_nightConsent_false_and_is_night() throws Exception {
            // given
            NotificationConsent consent = consentWith(true, false);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(consent));

            LocalTime nightTime = localTimeAt(22);
            try (MockedStatic<LocalTime> mockedStatic = mockStatic(LocalTime.class)) {
                mockedStatic.when(LocalTime::now).thenReturn(nightTime);

                // when
                notificationService.sendNotificationOnly(1L, NotificationType.CHAT, "제목", "내용");
            }

            // then
            then(fcmClient).should(never()).sendNotification(any(), any(), any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("타입별 수신 동의 필터링 (SUBSTITUTE / REPUTATION)")
    class TypeSpecificConsentTests {

        @Test
        @DisplayName("SUBSTITUTE 미동의 + SUBSTITUTE 타입 발송 → FCM 미호출")
        void skips_fcm_when_substitute_consent_is_false_and_type_is_substitute() throws Exception {
            // given
            NotificationConsent consent = consentWithAll(user, true, true, false, true);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(consent));

            // when
            notificationService.sendNotification(notificationRequest(1L, NotificationType.SUBSTITUTE));

            // then
            then(fcmClient).should(never()).sendNotification(any(), any(), any());
            then(notificationRepository).should().save(any());
        }

        @Test
        @DisplayName("SUBSTITUTE 미동의 + GENERAL 타입 발송 → FCM 호출됨 (타입 차단 누수 없음)")
        void sends_fcm_when_substitute_consent_is_false_but_type_is_general() throws Exception {
            // given
            NotificationConsent consent = consentWithAll(user, true, true, false, true);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(consent));

            // when
            notificationService.sendNotification(notificationRequest(1L, NotificationType.GENERAL));

            // then
            then(fcmClient).should().sendNotification(eq("test-device-token"), any(), any());
        }

        @Test
        @DisplayName("REPUTATION 미동의 + REPUTATION 타입 발송 → FCM 미호출")
        void skips_fcm_when_reputation_consent_is_false_and_type_is_reputation() throws Exception {
            // given
            NotificationConsent consent = consentWithAll(user, true, true, true, false);
            given(notificationConsentQueryRepository.findByUser(user)).willReturn(Optional.of(consent));

            // when
            notificationService.sendNotification(notificationRequest(1L, NotificationType.REPUTATION));

            // then
            then(fcmClient).should(never()).sendNotification(any(), any(), any());
            then(notificationRepository).should().save(any());
        }

        @Test
        @DisplayName("배치 SUBSTITUTE 발송: 일부 사용자만 SUBSTITUTE 미동의 → 미동의자 제외")
        void batch_excludes_users_without_substitute_consent_for_substitute_type() throws Exception {
            // given
            User userA = mock(User.class);
            given(userA.getId()).willReturn(10L);

            User userB = mock(User.class);
            given(userB.getId()).willReturn(20L);

            given(userQueryRepository.findAllById(List.of(10L, 20L)))
                .willReturn(List.of(userA, userB));

            FcmDeviceToken tokenA = mock(FcmDeviceToken.class);
            given(tokenA.getDeviceToken()).willReturn("token-A");
            given(tokenA.getUser()).willReturn(userA);

            FcmDeviceToken tokenB = mock(FcmDeviceToken.class);
            given(tokenB.getDeviceToken()).willReturn("token-B");
            given(tokenB.getUser()).willReturn(userB);

            given(userFCMDeviceTokenQueryRepository.findByUsers(anyList()))
                .willReturn(List.of(tokenA, tokenB));

            // userA는 SUBSTITUTE 동의, userB는 SUBSTITUTE 미동의
            NotificationConsent consentA = consentWithAll(userA, true, true, true, true);
            NotificationConsent consentB = consentWithAll(userB, true, true, false, true);
            given(notificationConsentQueryRepository.findByUsers(anyList()))
                .willReturn(List.of(consentA, consentB));

            com.google.firebase.messaging.BatchResponse batchResponse =
                mock(com.google.firebase.messaging.BatchResponse.class);
            com.google.firebase.messaging.SendResponse sendResponse =
                mock(com.google.firebase.messaging.SendResponse.class);
            given(sendResponse.isSuccessful()).willReturn(true);
            given(batchResponse.getResponses()).willReturn(List.of(sendResponse));
            given(fcmClient.sendMultipleNotifications(anyList(), any(), any())).willReturn(batchResponse);

            // when
            notificationService.sendMultipleNotifications(batchRequest(List.of(10L, 20L), NotificationType.SUBSTITUTE));

            // then: Notification 레코드는 두 사용자 모두 저장
            then(notificationRepository).should().saveAll(argThat(list -> ((List<?>) list).size() == 2));

            // then: FCM 배치는 SUBSTITUTE 동의자 토큰만 포함
            then(fcmClient).should().sendMultipleNotifications(
                argThat(tokens -> {
                    @SuppressWarnings("unchecked")
                    List<String> tokenList = (List<String>) tokens;
                    return tokenList.size() == 1 && tokenList.contains("token-A");
                }),
                any(), any()
            );
        }
    }
}
