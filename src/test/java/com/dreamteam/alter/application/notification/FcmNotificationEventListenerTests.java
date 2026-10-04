package com.dreamteam.alter.application.notification;

import com.dreamteam.alter.adapter.inbound.common.dto.FcmNotificationRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.FcmBatchNotificationRequestDto;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.notification.type.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import javax.sql.DataSource;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(FcmNotificationEventListenerTests.Config.class)
class FcmNotificationEventListenerTests {
    @Configuration
    @EnableTransactionManagement
    static class Config {
        @Bean DataSource dataSource() { return new EmbeddedDatabaseBuilder().generateUniqueName(true).setType(EmbeddedDatabaseType.H2).build(); }
        @Bean PlatformTransactionManager transactionManager(DataSource source) { return new DataSourceTransactionManager(source); }
        @Bean NotificationService notificationService() { return mock(NotificationService.class); }
        @Bean FcmNotificationEventListener listener(NotificationService service) { return new FcmNotificationEventListener(service); }
    }
    @Autowired NotificationService service;
    @Autowired ApplicationEventPublisher publisher;
    @Autowired PlatformTransactionManager transactionManager;
    @BeforeEach void resetService() { reset(service); }

    private FcmNotificationEvent event() {
        return new FcmNotificationEvent(FcmNotificationRequestDto.of(1L, TokenScope.APP,
            NotificationType.POSTING_APPLICATION, "지원 결과", "불합격"));
    }

    @Test void notificationRunsAfterCommit() {
        FcmNotificationEvent event = event();
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            publisher.publishEvent(event);
            verifyNoInteractions(service);
        });
        verify(service).sendNotificationAfterCommit(event.request());
    }

    @Test void rollbackDoesNotSendNotification() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            publisher.publishEvent(event());
            status.setRollbackOnly();
        });
        verifyNoInteractions(service);
    }

    @Test void deliveryFailureDoesNotFailCommittedTransaction() {
        doThrow(new IllegalStateException("FCM 실패")).when(service).sendNotificationAfterCommit(any());
        assertThatCode(() -> new TransactionTemplate(transactionManager)
            .executeWithoutResult(status -> publisher.publishEvent(event()))).doesNotThrowAnyException();
        verify(service).sendNotificationAfterCommit(any());
    }

    private FcmBatchNotificationEvent batchEvent() {
        return new FcmBatchNotificationEvent(FcmBatchNotificationRequestDto.of(List.of(1L, 2L), TokenScope.APP,
            NotificationType.POSTING_APPLICATION, "지원 결과", "불합격"));
    }

    @Test void batchNotificationRunsOnceAfterCommit() {
        FcmBatchNotificationEvent event = batchEvent();
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            publisher.publishEvent(event);
            verifyNoInteractions(service);
        });
        verify(service).sendMultipleNotificationsAfterCommit(event.request());
        verifyNoMoreInteractions(service);
    }

    @Test void rollbackDoesNotSendBatchNotification() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            publisher.publishEvent(batchEvent());
            status.setRollbackOnly();
        });
        verifyNoInteractions(service);
    }

    @Test void batchDeliveryFailureDoesNotFailCommittedTransaction() {
        doThrow(new IllegalStateException("FCM 실패")).when(service).sendMultipleNotificationsAfterCommit(any());
        assertThatCode(() -> new TransactionTemplate(transactionManager)
            .executeWithoutResult(status -> publisher.publishEvent(batchEvent()))).doesNotThrowAnyException();
        verify(service).sendMultipleNotificationsAfterCommit(any());
    }
}
