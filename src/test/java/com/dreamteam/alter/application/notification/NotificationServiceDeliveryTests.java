package com.dreamteam.alter.application.notification;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmBatchNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.UserFcmDeviceTokenQueryRepository;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.entity.NotificationConsent;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationConsentQueryRepository;
import com.dreamteam.alter.domain.notification.port.outbound.NotificationRepository;
import com.dreamteam.alter.domain.notification.type.NotificationType;
import com.dreamteam.alter.domain.user.entity.FcmDeviceToken;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserFcmDeviceTokenRepository;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.user.type.DevicePlatformType;
import com.dreamteam.alter.domain.user.type.UserGender;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.SendResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceDeliveryTests {
    @Mock FcmClient fcmClient;
    @Mock UserFcmDeviceTokenRepository tokens;
    @Mock UserFcmDeviceTokenQueryRepository tokenQueries;
    @Mock NotificationRepository notifications;
    @Mock UserQueryRepository userQueries;
    @Mock EntityManager em;
    @Mock NotificationConsentQueryRepository consentQueries;
    @InjectMocks NotificationService service;

    private List<User> users;
    private List<FcmDeviceToken> devices;

    private void recipients(int count) {
        users = IntStream.range(0, count).mapToObj(i -> {
            User user = User.create("01000000000", "encoded", "수신자", "recipient" + i,
                UserGender.GENDER_MALE, "19990101", null);
            ReflectionTestUtils.setField(user, "id", (long) i + 1);
            return user;
        }).toList();
        devices = users.stream().map(user -> FcmDeviceToken.create(user, "token-" + user.getId(),
            DevicePlatformType.ANDROID)).toList();
        when(userQueries.findAllById(ids())).thenReturn(users);
        if (count > 0) {
            when(tokenQueries.findByUsers(users)).thenReturn(devices);
            when(consentQueries.findByUsers(users)).thenReturn(users.stream()
                .map(user -> NotificationConsent.create(user, true, true)).toList());
        }
    }

    private List<Long> ids() { return users.stream().map(User::getId).toList(); }

    private FcmBatchNotificationRequestDto batch(NotificationType type) {
        return FcmBatchNotificationRequestDto.of(ids(), TokenScope.APP, type, "제목", "본문");
    }

    private BatchResponse success(int count) {
        SendResponse sent = mock(SendResponse.class);
        when(sent.isSuccessful()).thenReturn(true);
        BatchResponse response = mock(BatchResponse.class);
        when(response.getResponses()).thenReturn(java.util.Collections.nCopies(count, sent));
        return response;
    }

    private FirebaseMessagingException firebaseFailure(MessagingErrorCode code) {
        FirebaseMessagingException failure = mock(FirebaseMessagingException.class);
        when(failure.getMessagingErrorCode()).thenReturn(code);
        return failure;
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 500, 501, 1001})
    void sendsAtMost500TokensInOrderWithOneRoundOfDatabaseOperations(int count) throws Exception {
        recipients(count);
        List<List<String>> batches = new ArrayList<>();
        if (count > 0) {
            when(fcmClient.sendMultipleNotifications(anyList(), anyString(), anyString())).thenAnswer(call -> {
                List<String> batch = call.getArgument(0);
                assertThat(batch).hasSizeBetween(1, 500);
                batches.add(List.copyOf(batch));
                return success(batch.size());
            });
        }

        service.sendMultipleNotificationsAfterCommit(batch(NotificationType.POSTING_APPLICATION));

        assertThat(batches).hasSize((count + 499) / 500);
        assertThat(batches.stream().flatMap(List::stream).toList())
            .containsExactlyElementsOf(devices.stream().map(FcmDeviceToken::getDeviceToken).toList());
        assertThat(devices).allSatisfy(device -> assertThat(device.getLastNotificationSentAt()).isNotNull());
        verify(userQueries).findAllById(ids());
        if (count > 0) {
            verify(tokenQueries).findByUsers(users);
            verify(consentQueries).findByUsers(users);
            verify(notifications).saveAll(argThat(saved -> saved.size() == count));
        }
        verifyNoMoreInteractions(userQueries, tokenQueries, consentQueries, notifications);
        verifyNoInteractions(tokens);
    }

    @Test
    void filtersConsentBeforeSplittingAndStillSavesEveryNotification() throws Exception {
        recipients(1001);
        when(consentQueries.findByUsers(users)).thenReturn(IntStream.range(0, users.size())
            .mapToObj(i -> NotificationConsent.create(users.get(i), i % 2 == 0, true)).toList());
        List<List<String>> batches = new ArrayList<>();
        when(fcmClient.sendMultipleNotifications(anyList(), anyString(), anyString())).thenAnswer(call -> {
            List<String> batch = call.getArgument(0);
            batches.add(List.copyOf(batch));
            return success(batch.size());
        });

        service.sendMultipleNotificationsAfterCommit(batch(NotificationType.POSTING_APPLICATION));

        assertThat(batches).extracting(List::size).containsExactly(500, 1);
        assertThat(batches.stream().flatMap(List::stream).toList()).containsExactlyElementsOf(
            IntStream.range(0, devices.size()).filter(i -> i % 2 == 0)
                .mapToObj(i -> devices.get(i).getDeviceToken()).toList());
        verify(notifications).saveAll(argThat(saved -> saved.size() == 1001));
    }

    @Test
    void matchesResponsesWithinEachBatchAndDeletesAllInvalidTokensOnce() throws Exception {
        recipients(1001);
        List<FcmDeviceToken> invalid = new ArrayList<>();
        when(fcmClient.sendMultipleNotifications(anyList(), anyString(), anyString())).thenAnswer(call -> {
            List<String> batch = call.getArgument(0);
            List<SendResponse> responses = new ArrayList<>();
            for (int i = 0; i < batch.size(); i++) {
                SendResponse response = mock(SendResponse.class);
                if (i == 0) {
                    doReturn(firebaseFailure(MessagingErrorCode.UNREGISTERED)).when(response).getException();
                    invalid.add(devices.stream().filter(d -> d.getDeviceToken().equals(batch.getFirst())).findFirst().orElseThrow());
                } else if (i == 1) {
                    doReturn(firebaseFailure(MessagingErrorCode.UNAVAILABLE)).when(response).getException();
                } else {
                    when(response.isSuccessful()).thenReturn(true);
                }
                responses.add(response);
            }
            BatchResponse result = mock(BatchResponse.class);
            when(result.getResponses()).thenReturn(responses);
            return result;
        });

        service.sendMultipleNotificationsAfterCommit(batch(NotificationType.POSTING_APPLICATION));

        verify(tokens).deleteAll(invalid);
        verifyNoMoreInteractions(tokens);
        assertThat(invalid).containsExactly(devices.get(0), devices.get(500), devices.get(1000));
        IntStream.range(0, devices.size()).forEach(i -> {
            if (i % 500 < 2) assertThat(devices.get(i).getLastNotificationSentAt()).isNull();
            else assertThat(devices.get(i).getLastNotificationSentAt()).isNotNull();
        });
    }

    @Test
    void postingBatchContinuesAfterFirebaseFailure() throws Exception {
        recipients(501);
        when(fcmClient.sendMultipleNotifications(anyList(), anyString(), anyString()))
            .thenThrow(mock(FirebaseMessagingException.class)).thenReturn(success(1));

        assertThatCode(() -> service.sendMultipleNotificationsAfterCommit(batch(NotificationType.POSTING_APPLICATION)))
            .doesNotThrowAnyException();

        verify(fcmClient, times(2)).sendMultipleNotifications(anyList(), anyString(), anyString());
        assertThat(devices.subList(0, 500)).allSatisfy(d -> assertThat(d.getLastNotificationSentAt()).isNull());
        assertThat(devices.get(500).getLastNotificationSentAt()).isNotNull();
        verify(notifications).saveAll(argThat(saved -> saved.size() == 501));
    }

    @Test
    void postingBatchContinuesAfterRuntimeFailureAtFcmCall() throws Exception {
        recipients(501);
        when(fcmClient.sendMultipleNotifications(anyList(), anyString(), anyString()))
            .thenThrow(new IllegalStateException("FCM unavailable")).thenReturn(success(1));
        assertThatCode(() -> service.sendMultipleNotificationsAfterCommit(batch(NotificationType.POSTING_APPLICATION)))
            .doesNotThrowAnyException();
        verify(fcmClient, times(2)).sendMultipleNotifications(anyList(), anyString(), anyString());
    }

    private FcmNotificationRequestDto singleRecipient() {
        User user = User.create("01000000000", "encoded", "수신자", "recipient", UserGender.GENDER_MALE, "19990101", null);
        when(userQueries.findById(1L)).thenReturn(Optional.of(user));
        when(tokenQueries.findByUser(user)).thenReturn(Optional.of(FcmDeviceToken.create(user, "token", DevicePlatformType.ANDROID)));
        when(consentQueries.findByUser(user)).thenReturn(Optional.of(NotificationConsent.create(user, true, true)));
        return FcmNotificationRequestDto.of(1L, TokenScope.APP, NotificationType.POSTING_APPLICATION, "제목", "본문");
    }

    @Test
    void postingSingleKeepsHistoryOnFirebaseFailureAndDeletesInvalidToken() throws Exception {
        var request = singleRecipient();
        doThrow(firebaseFailure(MessagingErrorCode.INVALID_ARGUMENT)).when(fcmClient).sendNotification(anyString(), anyString(), anyString());
        assertThatCode(() -> service.sendNotificationAfterCommit(request)).doesNotThrowAnyException();
        verify(notifications).save(any());
        verify(tokens).delete(any());
    }

    @Test
    void postingSingleKeepsHistoryOnRuntimeFailureAtFcmCall() throws Exception {
        var request = singleRecipient();
        doThrow(new IllegalStateException("FCM unavailable")).when(fcmClient).sendNotification(anyString(), anyString(), anyString());
        assertThatCode(() -> service.sendNotificationAfterCommit(request)).doesNotThrowAnyException();
        verify(notifications).save(any());
    }

    @Test
    void databaseFailureDuringResponseProcessingIsPropagated() throws Exception {
        recipients(1);
        SendResponse rejected = mock(SendResponse.class);
        doReturn(firebaseFailure(MessagingErrorCode.UNREGISTERED)).when(rejected).getException();
        BatchResponse result = mock(BatchResponse.class);
        when(result.getResponses()).thenReturn(List.of(rejected));
        when(fcmClient.sendMultipleNotifications(anyList(), anyString(), anyString())).thenReturn(result);
        var failure = new DataAccessResourceFailureException("DB unavailable");
        doThrow(failure).when(tokens).deleteAll(anyList());
        assertThatThrownBy(() -> service.sendMultipleNotificationsAfterCommit(batch(NotificationType.POSTING_APPLICATION)))
            .isSameAs(failure);
    }

    @Test
    void databaseSaveFailureIsPropagatedWithoutSendingFcm() {
        recipients(1);
        // 동의 조회는 저장 실패 이후 실행되지 않는다.
        reset(consentQueries);
        var failure = new DataAccessResourceFailureException("DB unavailable");
        when(notifications.saveAll(anyList())).thenThrow(failure);
        assertThatThrownBy(() -> service.sendMultipleNotificationsAfterCommit(batch(NotificationType.POSTING_APPLICATION)))
            .isSameAs(failure);
        verifyNoInteractions(fcmClient);
    }

    @Test
    void allBlockedRecipientsStillGetHistoryWithoutAnEmptyFcmBatch() {
        recipients(501);
        when(consentQueries.findByUsers(users)).thenReturn(users.stream()
            .map(user -> NotificationConsent.create(user, false, false)).toList());
        service.sendMultipleNotificationsAfterCommit(batch(NotificationType.POSTING_APPLICATION));
        verify(notifications).saveAll(argThat(saved -> saved.size() == 501));
        verifyNoInteractions(fcmClient, tokens);
    }

    @Test
    void singleDatabaseLookupFailureIsPropagated() {
        var failure = new DataAccessResourceFailureException("DB unavailable");
        when(userQueries.findById(1L)).thenThrow(failure);
        var request = FcmNotificationRequestDto.of(1L, TokenScope.APP, NotificationType.POSTING_APPLICATION, "제목", "본문");
        assertThatThrownBy(() -> service.sendNotificationAfterCommit(request)).isSameAs(failure);
        verifyNoInteractions(fcmClient, notifications);
    }

    @Test
    void singleInvalidTokenDeletionFailureIsPropagated() throws Exception {
        var request = singleRecipient();
        doThrow(firebaseFailure(MessagingErrorCode.UNREGISTERED)).when(fcmClient).sendNotification(anyString(), anyString(), anyString());
        var failure = new DataAccessResourceFailureException("DB unavailable");
        doThrow(failure).when(tokens).delete(any());
        assertThatThrownBy(() -> service.sendNotificationAfterCommit(request)).isSameAs(failure);
    }

    @Test
    void afterCommitSingleKeepsHistoryWhileSynchronousSingleStillFails() throws Exception {
        singleRecipient();
        doThrow(mock(FirebaseMessagingException.class)).when(fcmClient).sendNotification(anyString(), anyString(), anyString());
        var request = FcmNotificationRequestDto.of(1L, TokenScope.APP, NotificationType.GENERAL, "제목", "본문");
        assertThatCode(() -> service.sendNotificationAfterCommit(request)).doesNotThrowAnyException();
        assertThatThrownBy(() -> service.sendNotification(request)).isInstanceOf(CustomException.class);
    }

    @Test
    void afterCommitBatchKeepsHistoryWhileSynchronousPostingBatchStillFails() throws Exception {
        recipients(501);
        when(fcmClient.sendMultipleNotifications(anyList(), anyString(), anyString())).thenThrow(mock(FirebaseMessagingException.class));
        assertThatCode(() -> service.sendMultipleNotificationsAfterCommit(batch(NotificationType.GENERAL)))
            .doesNotThrowAnyException();
        assertThatThrownBy(() -> service.sendMultipleNotifications(batch(NotificationType.POSTING_APPLICATION)))
            .isInstanceOf(CustomException.class);
        verify(fcmClient, times(3)).sendMultipleNotifications(anyList(), anyString(), anyString());
    }

    @Test
    void chatRemainsBestEffortAndUsesBatchLimitWithoutSavingHistory() throws Exception {
        recipients(501);
        when(fcmClient.sendMultipleNotifications(anyList(), anyString(), anyString()))
            .thenThrow(mock(FirebaseMessagingException.class)).thenReturn(success(1));
        service.sendNotificationOnlyToMany(ids(), NotificationType.CHAT, "제목", "본문");
        verify(fcmClient, times(2)).sendMultipleNotifications(anyList(), anyString(), anyString());
        verifyNoInteractions(notifications);
    }

    @Test
    void chatStillPropagatesRuntimeFcmFailure() throws Exception {
        recipients(501);
        var failure = new IllegalStateException("FCM runtime failure");
        when(fcmClient.sendMultipleNotifications(anyList(), anyString(), anyString())).thenThrow(failure);
        assertThatThrownBy(() -> service.sendNotificationOnlyToMany(ids(), NotificationType.CHAT, "제목", "본문"))
            .isSameAs(failure);
        verify(fcmClient).sendMultipleNotifications(anyList(), anyString(), anyString());
        verifyNoInteractions(notifications);
    }
}
