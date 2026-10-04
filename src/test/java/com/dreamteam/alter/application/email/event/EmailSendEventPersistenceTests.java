package com.dreamteam.alter.application.email.event;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.dreamteam.alter.domain.email.entity.EmailSendLog;
import com.dreamteam.alter.domain.email.port.outbound.EmailClient;
import com.dreamteam.alter.domain.email.port.outbound.EmailSendLogRepository;
import com.dreamteam.alter.domain.email.port.outbound.EmailVerificationSessionStoreRepository;
import com.dreamteam.alter.domain.email.type.EmailSendStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class EmailSendEventPersistenceTests {
    private static final String EMAIL = "synthetic-event@example.com";
    private static final String CODE = "673219";
    @Autowired EmailSendLogRepository sendLogs;
    @Autowired ApplicationEventPublisher publisher;
    @Autowired PlatformTransactionManager transactionManager;
    @MockitoBean EmailClient emailClient;
    @MockitoBean EmailVerificationSessionStoreRepository sessions;
    private final Logger logger = (Logger) LoggerFactory.getLogger(EmailSendEventListener.class);
    private ListAppender<ILoggingEvent> logs;

    @BeforeEach
    void captureLogs() {
        logs = new ListAppender<>();
        logs.start();
        logger.addAppender(logs);
    }

    @AfterEach
    void removeAppender() { logger.detachAppender(logs); }

    @Test
    void finalFailureCommitsFailedLogDeletesCodeAndDoesNotExposePrivatePayload() {
        String raw = "private provider message " + EMAIL + " " + CODE;
        doThrow(new IllegalStateException(raw)).when(emailClient).sendVerificationCode(EMAIL, CODE);
        Long id = tx().execute(status -> {
            EmailSendLog item = sendLogs.save(EmailSendLog.create(EMAIL));
            publisher.publishEvent(new EmailSendEvent(item.getId(), EMAIL, CODE));
            return item.getId();
        });

        await().atMost(Duration.ofSeconds(8)).untilAsserted(() ->
            assertThat(statusOf(id)).isEqualTo(EmailSendStatus.FAILED));
        verify(emailClient, times(3)).sendVerificationCode(EMAIL, CODE);
        verify(sessions).deleteCode(EMAIL);
        verify(sessions, never()).createVerificationSession(anyString(), any());
        assertThat(logs.list).hasSize(1);
        assertThat(logs.list.getFirst().getFormattedMessage()).doesNotContain(EMAIL, CODE, raw);
        assertThat(logs.list.getFirst().getThrowableProxy()).isNull();
    }

    @Test
    void successCommitsSentLogWithoutRemovingTheCode() {
        Long id = tx().execute(status -> {
            EmailSendLog item = sendLogs.save(EmailSendLog.create(EMAIL));
            publisher.publishEvent(new EmailSendEvent(item.getId(), EMAIL, CODE));
            return item.getId();
        });
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
            assertThat(statusOf(id)).isEqualTo(EmailSendStatus.SENT));
        verify(emailClient).sendVerificationCode(EMAIL, CODE);
        verify(sessions, never()).deleteCode(anyString());
        verify(sessions, never()).createVerificationSession(anyString(), any());
    }

    @Test
    void businessRollbackDoesNotSendEmailOrKeepPendingLog() {
        Long id = tx().execute(status -> {
            EmailSendLog item = sendLogs.save(EmailSendLog.create(EMAIL));
            publisher.publishEvent(new EmailSendEvent(item.getId(), EMAIL, CODE));
            status.setRollbackOnly();
            return item.getId();
        });
        Boolean absent = tx().execute(status -> sendLogs.findById(id).isEmpty());
        assertThat(absent).isTrue();
        verifyNoInteractions(emailClient, sessions);
    }

    private EmailSendStatus statusOf(Long id) {
        return tx().execute(status -> sendLogs.findById(id).orElseThrow().getStatus());
    }

    private TransactionTemplate tx() { return new TransactionTemplate(transactionManager); }
}
