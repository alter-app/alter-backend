package com.dreamteam.alter.application.email.usecase;

import com.dreamteam.alter.application.email.event.EmailSendEvent;
import com.dreamteam.alter.application.email.service.VerificationCodeGenerator;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.email.command.SendEmailVerificationCodeCommand;
import com.dreamteam.alter.domain.email.command.VerifyEmailVerificationCodeCommand;
import com.dreamteam.alter.domain.email.entity.EmailSendLog;
import com.dreamteam.alter.domain.email.port.outbound.EmailSendLogRepository;
import com.dreamteam.alter.domain.email.port.outbound.EmailVerificationSessionStoreRepository;
import com.dreamteam.alter.domain.email.type.EmailSendStatus;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationUseCaseTests {
    private static final String EMAIL = "synthetic-verification@example.com";
    @Mock UserQueryRepository users;
    @Mock EmailVerificationSessionStoreRepository sessions;
    @Mock EmailSendLogRepository sendLogs;
    @Mock VerificationCodeGenerator generator;
    @Mock ApplicationEventPublisher publisher;
    @InjectMocks SendEmailVerificationCode send;
    @InjectMocks VerifyEmailVerificationCode verifyCode;

    @BeforeEach
    void configureTtls() {
        ReflectionTestUtils.setField(send, "codeTtlSeconds", 300L);
        ReflectionTestUtils.setField(send, "cooldownSeconds", 30L);
        ReflectionTestUtils.setField(verifyCode, "codeTtlSeconds", 300L);
        ReflectionTestUtils.setField(verifyCode, "verifiedTtlSeconds", 900L);
        ReflectionTestUtils.setField(verifyCode, "maxAttempts", 5);
    }

    @Test
    void duplicateEmailDoesNotCreateCodeOrSendEvent() {
        when(users.findByEmail(EMAIL)).thenReturn(Optional.of(mock(User.class)));
        assertThatThrownBy(() -> send.execute(new SendEmailVerificationCodeCommand(EMAIL)))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.EMAIL_DUPLICATED);
        verifyNoInteractions(sessions, generator, sendLogs, publisher);
    }

    @Test
    void resendDuringCooldownIsRejectedWithoutReplacingCode() {
        when(sessions.isCooldown(EMAIL)).thenReturn(true);
        assertThatThrownBy(() -> send.execute(new SendEmailVerificationCodeCommand(EMAIL)))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.TOO_MANY_REQUESTS);
        verify(sessions, never()).saveCode(any(), any(), any());
        verifyNoInteractions(generator, sendLogs, publisher);
    }

    @Test
    void sendAndResendAfterCooldownCreatePendingRequestsWithoutVerifiedSession() {
        when(generator.generate()).thenReturn("123456", "654321");
        when(sendLogs.save(any())).thenAnswer(invocation -> {
            EmailSendLog log = invocation.getArgument(0);
            assertThat(log.getStatus()).isEqualTo(EmailSendStatus.PENDING);
            ReflectionTestUtils.setField(log, "id", 1L);
            return log;
        });
        send.execute(new SendEmailVerificationCodeCommand(EMAIL));
        send.execute(new SendEmailVerificationCodeCommand(EMAIL));
        verify(sessions).saveCode(EMAIL, "123456", Duration.ofSeconds(300));
        verify(sessions).saveCode(EMAIL, "654321", Duration.ofSeconds(300));
        verify(sessions, times(2)).markCooldown(EMAIL, Duration.ofSeconds(30));
        verify(publisher, times(2)).publishEvent(any(EmailSendEvent.class));
        verify(sessions, never()).createVerificationSession(any(), any());
    }

    @Test
    void expiredOrDeletedCodeCannotCreateVerificationSession() {
        when(sessions.findCode(EMAIL)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> verifyCode.execute(new VerifyEmailVerificationCodeCommand(EMAIL, "123456")))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.ILLEGAL_ARGUMENT);
        verify(sessions, never()).createVerificationSession(any(), any());
    }

    @Test
    void wrongCodeCountsAttemptButDoesNotCreateVerificationSession() {
        when(sessions.findCode(EMAIL)).thenReturn(Optional.of("123456"));
        when(sessions.incrementAttempt(EMAIL, Duration.ofSeconds(300))).thenReturn(1L);
        assertThatThrownBy(() -> verifyCode.execute(new VerifyEmailVerificationCodeCommand(EMAIL, "000000")))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.ILLEGAL_ARGUMENT);
        verify(sessions, never()).deleteCode(any());
        verify(sessions, never()).createVerificationSession(any(), any());
    }

    @Test
    void fifthWrongAttemptDeletesCodeWithoutCreatingSession() {
        when(sessions.findCode(EMAIL)).thenReturn(Optional.of("123456"));
        when(sessions.incrementAttempt(EMAIL, Duration.ofSeconds(300))).thenReturn(5L);
        assertThatThrownBy(() -> verifyCode.execute(new VerifyEmailVerificationCodeCommand(EMAIL, "000000")))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.ILLEGAL_ARGUMENT);
        verify(sessions).deleteCode(EMAIL);
        verify(sessions, never()).createVerificationSession(any(), any());
    }

    @Test
    void matchingCodeIsConsumedBeforeCreatingBoundedVerificationSession() {
        when(sessions.findCode(EMAIL)).thenReturn(Optional.of("123456"));
        when(sessions.createVerificationSession(EMAIL, Duration.ofSeconds(900))).thenReturn("synthetic-session");
        var result = verifyCode.execute(new VerifyEmailVerificationCodeCommand(EMAIL, "123456"));
        assertThat(result.sessionId()).isEqualTo("synthetic-session");
        var order = inOrder(sessions);
        order.verify(sessions).deleteCode(EMAIL);
        order.verify(sessions).createVerificationSession(EMAIL, Duration.ofSeconds(900));
    }
}
