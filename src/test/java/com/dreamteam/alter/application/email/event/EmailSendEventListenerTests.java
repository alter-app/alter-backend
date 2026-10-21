package com.dreamteam.alter.application.email.event;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.email.entity.EmailSendLog;
import com.dreamteam.alter.domain.email.port.outbound.EmailClient;
import com.dreamteam.alter.domain.email.port.outbound.EmailSendLogRepository;
import com.dreamteam.alter.domain.email.port.outbound.EmailVerificationSessionStoreRepository;
import com.dreamteam.alter.domain.email.type.EmailSendStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailSendEventListenerTests {
    private static final String EMAIL = "synthetic-event@example.com";
    private static final String CODE = "673219";
    @Mock EmailSendLogRepository sendLogs;
    @Mock EmailClient emailClient;
    @Mock EmailVerificationSessionStoreRepository sessions;
    @InjectMocks EmailSendEventListener listener;
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
    void finalFailureMarksFailedDeletesCodeAndDoesNotExposePrivatePayload() {
        String raw = "private provider message " + EMAIL + " " + CODE;
        EmailSendLog item = EmailSendLog.create(EMAIL);
        when(sendLogs.findById(1L)).thenReturn(Optional.of(item));
        doThrow(new CustomException(ErrorCode.EXTERNAL_API_ERROR, raw)).when(emailClient).sendVerificationCode(EMAIL, CODE);

        listener.handleEmailSendEvent(new EmailSendEvent(1L, EMAIL, CODE));

        assertThat(item.getStatus()).isEqualTo(EmailSendStatus.FAILED);
        verify(emailClient, times(3)).sendVerificationCode(EMAIL, CODE);
        verify(sessions).deleteCode(EMAIL);
        verify(sendLogs).save(item);
        assertThat(logs.list).hasSize(1);
        assertThat(logs.list.getFirst().getFormattedMessage()).doesNotContain(EMAIL, CODE, raw);
        assertThat(logs.list.getFirst().getThrowableProxy()).isNull();
    }

    @Test
    void successMarksSentWithoutRemovingTheCode() {
        EmailSendLog item = EmailSendLog.create(EMAIL);
        when(sendLogs.findById(1L)).thenReturn(Optional.of(item));

        listener.handleEmailSendEvent(new EmailSendEvent(1L, EMAIL, CODE));

        assertThat(item.getStatus()).isEqualTo(EmailSendStatus.SENT);
        verify(emailClient).sendVerificationCode(EMAIL, CODE);
        verify(sessions, never()).deleteCode(any());
        verify(sendLogs).save(item);
        assertThat(logs.list).isEmpty();
    }
}
