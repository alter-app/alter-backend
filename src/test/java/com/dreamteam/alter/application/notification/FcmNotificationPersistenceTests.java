package com.dreamteam.alter.application.notification;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.FcmBatchNotificationRequestDto;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.type.NotificationType;
import com.dreamteam.alter.domain.notification.entity.NotificationConsent;
import com.dreamteam.alter.domain.user.entity.FcmDeviceToken;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserRepository;
import com.dreamteam.alter.domain.user.type.UserGender;
import com.dreamteam.alter.domain.user.type.DevicePlatformType;
import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class FcmNotificationPersistenceTests {
    @Autowired UserRepository users;
    @Autowired EntityManager em;
    @Autowired ApplicationEventPublisher publisher;
    @Autowired PlatformTransactionManager transactionManager;
    @MockitoBean FcmClient fcmClient;

    @Test
    void afterCommitNotificationIsVisibleInNextTransaction() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        Long userId = tx.execute(status -> users.save(User.create("01029500000", "encoded", "알림 검증",
            "notification" + System.nanoTime(), UserGender.GENDER_MALE, "19990101", null)).getId());
        tx.executeWithoutResult(status -> publisher.publishEvent(new FcmNotificationEvent(
            FcmNotificationRequestDto.of(userId, TokenScope.APP, NotificationType.POSTING_APPLICATION,
                "지원 결과를 안내드립니다", "ALT295 테스트 업장 지원 결과: 불합격"))));
        Long saved = tx.execute(status -> em.createQuery(
            "select count(n) from Notification n where n.targetUser.id = :userId and n.body = :body", Long.class)
            .setParameter("userId", userId).setParameter("body", "ALT295 테스트 업장 지원 결과: 불합격").getSingleResult());
        assertThat(saved).isEqualTo(1);
    }

    @Test
    void afterCommitBatchNotificationsAreVisibleInNextTransaction() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        List<Long> userIds = tx.execute(status -> List.of(
            users.save(User.create("01029500001", "encoded", "일괄 알림1", "batch1" + System.nanoTime(),
                UserGender.GENDER_MALE, "19990101", null)).getId(),
            users.save(User.create("01029500002", "encoded", "일괄 알림2", "batch2" + System.nanoTime(),
                UserGender.GENDER_MALE, "19990101", null)).getId()));
        String body = "ALT295 테스트 업장 일괄 지원 결과: 불합격";
        tx.executeWithoutResult(status -> publisher.publishEvent(new FcmBatchNotificationEvent(
            FcmBatchNotificationRequestDto.of(userIds, TokenScope.APP, NotificationType.POSTING_APPLICATION,
                "지원 결과를 안내드립니다", body))));
        Long saved = tx.execute(status -> em.createQuery(
            "select count(n) from Notification n where n.targetUser.id in :userIds and n.body = :body", Long.class)
            .setParameter("userIds", userIds).setParameter("body", body).getSingleResult());
        assertThat(saved).isEqualTo(2);
    }

    private List<Long> recipients(TransactionTemplate tx) {
        return tx.execute(status -> java.util.stream.IntStream.range(0, 2).mapToObj(i -> {
            User user = users.save(User.create("010295" + String.format("%05d", i), "encoded", "푸시 실패 검증",
                "failure" + System.nanoTime(), UserGender.GENDER_MALE, "19990101", null));
            em.persist(FcmDeviceToken.create(user, "token-" + user.getId(), DevicePlatformType.ANDROID));
            em.persist(NotificationConsent.create(user, true, true));
            return user.getId();
        }).toList());
    }

    private Object failureEvent(List<Long> ids, boolean batch, String body) {
        return batch
            ? new FcmBatchNotificationEvent(FcmBatchNotificationRequestDto.of(ids, TokenScope.APP,
                NotificationType.POSTING_APPLICATION, "지원 결과", body))
            : new FcmNotificationEvent(FcmNotificationRequestDto.of(ids.getFirst(), TokenScope.APP,
                NotificationType.POSTING_APPLICATION, "지원 결과", body));
    }

    private long notificationCount(TransactionTemplate tx, List<Long> ids, String body) {
        return tx.execute(status -> em.createQuery(
            "select count(n) from Notification n where n.targetUser.id in :ids and n.body = :body", Long.class)
            .setParameter("ids", ids).setParameter("body", body).getSingleResult());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void fcmFailureDoesNotRollBackAfterCommitHistory(boolean batch) throws Exception {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        List<Long> ids = recipients(tx);
        FirebaseMessagingException failure = mock(FirebaseMessagingException.class);
        if (batch) when(fcmClient.sendMultipleNotifications(anyList(), anyString(), anyString())).thenThrow(failure);
        else doThrow(failure).when(fcmClient).sendNotification(anyString(), anyString(), anyString());
        String body = "FCM 실패 후 이력 " + System.nanoTime();

        tx.executeWithoutResult(status -> publisher.publishEvent(failureEvent(ids, batch, body)));

        assertThat(notificationCount(tx, ids, body)).isEqualTo(batch ? 2 : 1);
        if (batch) verify(fcmClient).sendMultipleNotifications(anyList(), anyString(), anyString());
        else verify(fcmClient).sendNotification(anyString(), anyString(), anyString());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void runtimeFcmFailureDoesNotRollBackAfterCommitHistory(boolean batch) throws Exception {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        List<Long> ids = recipients(tx);
        var failure = new IllegalStateException("FCM 호출 실패");
        if (batch) when(fcmClient.sendMultipleNotifications(anyList(), anyString(), anyString())).thenThrow(failure);
        else doThrow(failure).when(fcmClient).sendNotification(anyString(), anyString(), anyString());
        String body = "FCM 런타임 실패 후 이력 " + System.nanoTime();

        tx.executeWithoutResult(status -> publisher.publishEvent(failureEvent(ids, batch, body)));

        assertThat(notificationCount(tx, ids, body)).isEqualTo(batch ? 2 : 1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void originalRollbackDoesNotCreateHistoryOrSendFcm(boolean batch) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        List<Long> ids = recipients(tx);
        String body = "롤백 후 이력 " + System.nanoTime();

        tx.executeWithoutResult(status -> {
            publisher.publishEvent(failureEvent(ids, batch, body));
            status.setRollbackOnly();
        });

        assertThat(notificationCount(tx, ids, body)).isZero();
        verifyNoInteractions(fcmClient);
    }
}
