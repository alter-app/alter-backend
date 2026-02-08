package com.dreamteam.alter.adapter.outbound.aws.ses;

import com.dreamteam.alter.application.email.properties.EmailAuthProperties;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.email.port.outbound.EmailSenderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SesEmailSenderAdapter implements EmailSenderPort {

    private final SesClient sesClient;
    private final EmailAuthProperties emailProperties;

    @Override
    public void sendVerificationCode(String toEmail, String code) {
        try {
            String subject = "[ALTER] 이메일 인증 코드";
            String bodyText = "인증 코드: " + code + "\n\n이 코드는 5분간 유효합니다.";

            SendEmailRequest request = SendEmailRequest.builder()
                    .source(emailProperties.getFrom())
                    .destination(Destination.builder().toAddresses(toEmail).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).build())
                            .body(Body.builder()
                                    .text(Content.builder().data(bodyText).build())
                                    .build())
                            .build())
                    .build();

            sesClient.sendEmail(request);
            log.info("Sent verification email to: {}", toEmail);

        } catch (SesException e) {
            log.error("Failed to send SES email to {}: {}", toEmail, e.awsErrorDetails().errorMessage());
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_SEND_FAILED);
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}: {}",toEmail, e.getMessage());
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_SEND_FAILED);
        }

    }
}
