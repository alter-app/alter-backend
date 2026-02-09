package com.dreamteam.alter.application.email.event;

import com.dreamteam.alter.domain.email.port.outbound.EmailSendLogPort;
import com.dreamteam.alter.domain.email.port.outbound.EmailSenderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSendEventListener {

    private final EmailSendLogPort emailSendLogPort;
    private final EmailSenderPort emailSenderPort;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmailSendEvent(EmailSendEvent event) {
        Long logId = event.getLogId();

        emailSendLogPort.findById(logId).ifPresent(logItem -> {
            try {
                log.info("Async sending email to: {}", logItem.getEmail());
                emailSenderPort.sendVerificationCode(logItem.getEmail(), logItem.getCode());
                logItem.markSent();
            } catch (Exception e) {
                log.error("Async failed to send email to: {}", logItem.getEmail(), e);
                logItem.markFailed();
            }
            emailSendLogPort.save(logItem);
        });
    }
}
