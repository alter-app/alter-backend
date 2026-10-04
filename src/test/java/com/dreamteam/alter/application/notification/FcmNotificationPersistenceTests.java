package com.dreamteam.alter.application.notification;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.FcmBatchNotificationRequestDto;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.type.NotificationType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserRepository;
import com.dreamteam.alter.domain.user.type.UserGender;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

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
}
