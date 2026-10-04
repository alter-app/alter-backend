package com.dreamteam.alter.application.email;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SesEmailSenderTests {
    private static final String EMAIL = "synthetic-recipient@example.com";
    private static final String CODE = "839217";
    private static final String RAW = "private provider message " + EMAIL;
    @Mock SesClient sesClient;
    @InjectMocks SesEmailSender sender;
    private final Logger logger = (Logger) LoggerFactory.getLogger(SesEmailSender.class);
    private ListAppender<ILoggingEvent> logs;

    @BeforeEach
    void captureLogs() {
        ReflectionTestUtils.setField(sender, "from", "no-reply@alter-app.com");
        ReflectionTestUtils.setField(sender, "region", "ap-northeast-2");
        logs = new ListAppender<>();
        logs.start();
        logger.addAppender(logs);
    }

    @AfterEach
    void removeAppender() { logger.detachAppender(logs); }

    @ParameterizedTest
    @CsvSource({"MessageRejected,400", "Throttling,429", "AccessDeniedException,403"})
    void sesFailureDoesNotLogRecipientCodeOrRawProviderMessage(String awsCode, int statusCode) {
        SesException.Builder builder = SesException.builder();
        builder.statusCode(statusCode);
        builder.awsErrorDetails(AwsErrorDetails.builder().errorCode(awsCode).errorMessage(RAW).build());
        SesException failure = (SesException) builder.build();
        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenThrow(failure);

        assertThatThrownBy(() -> sender.sendVerificationCode(EMAIL, CODE))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.EXTERNAL_API_ERROR);
        assertThat(logs.list).hasSize(1);
        assertThat(logs.list.getFirst().getFormattedMessage()).doesNotContain(EMAIL, CODE, RAW);
        assertThat(logs.list.getFirst().getFormattedMessage())
            .contains("region=ap-northeast-2", "status=" + statusCode, "awsErrorCode=" + awsCode, "errorType=SesException");
        assertThat(logs.list.getFirst().getThrowableProxy()).isNull();
    }

    @Test
    void unexpectedFailureDoesNotLogRecipientCodeOrRawMessage() {
        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenThrow(new IllegalStateException(RAW));

        assertThatThrownBy(() -> sender.sendVerificationCode(EMAIL, CODE))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.EXTERNAL_API_ERROR);
        assertThat(logs.list).hasSize(1);
        assertThat(logs.list.getFirst().getFormattedMessage()).doesNotContain(EMAIL, CODE, RAW);
        assertThat(logs.list.getFirst().getFormattedMessage()).contains("region=ap-northeast-2", "errorType=IllegalStateException");
        assertThat(logs.list.getFirst().getThrowableProxy()).isNull();
    }

    @Test
    void missingAwsErrorDetailsDoNotMaskExternalFailure() {
        SesException.Builder builder = SesException.builder();
        builder.statusCode(503);
        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenThrow((SesException) builder.build());
        assertThatThrownBy(() -> sender.sendVerificationCode(EMAIL, CODE))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.EXTERNAL_API_ERROR);
        assertThat(logs.list.getFirst().getFormattedMessage()).contains("status=503", "awsErrorCode=UNKNOWN")
            .doesNotContain(EMAIL, CODE);
    }
}
