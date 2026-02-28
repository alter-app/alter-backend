package com.dreamteam.alter.application.email;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.email.port.outbound.EmailClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SesEmailSender implements EmailClient {

    private final SesClient sesClient;

    @Value("${alter.email.from}")
    private String from;

    @Override
    public void sendVerificationCode(String toEmail, String code) {
        try {
            String subject = "[ALTER] 이메일 인증 코드";
            String bodyText = "인증 코드: " + code + "\n\n이 코드는 5분간 유효합니다.";

            SendEmailRequest request = SendEmailRequest.builder()
                    .source(from)
                    .destination(Destination.builder().toAddresses(toEmail).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).build())
                            .body(Body.builder()
                                    .text(Content.builder().data(bodyText).build())
                                    .build())
                            .build())
                    .build();

            sesClient.sendEmail(request);

        } catch (SesException e) {
            log.error("Failed to send SES email to {}: {}", toEmail, e.awsErrorDetails().errorMessage());
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "이메일 전송에 실패했습니다.");
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}: {}", toEmail, e.getMessage());
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "이메일 전송에 실패했습니다.");
        }
    }
}
