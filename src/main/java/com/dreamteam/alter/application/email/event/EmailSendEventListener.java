package com.dreamteam.alter.application.email.event;

import com.dreamteam.alter.domain.email.port.outbound.EmailSendLogRepository;
import com.dreamteam.alter.domain.email.port.outbound.EmailClient;
import com.dreamteam.alter.domain.email.port.outbound.EmailVerificationSessionStoreRepository;
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

    private final EmailSendLogRepository emailSendLogRepository;
    private final EmailClient emailClient;
    private final EmailVerificationSessionStoreRepository sessionStoreRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmailSendEvent(EmailSendEvent event) {
        Long logId = event.getLogId();

        emailSendLogRepository.findById(logId).ifPresent(logItem -> {
            try {
                emailClient.sendVerificationCode(event.getEmail(), event.getCode());
                logItem.markSent();
            } catch (Exception e) {
                log.error("Async failed to send email to: {}", event.getEmail(), e);
                logItem.markFailed();
                // 발송 실패 시 인증 코드 삭제
                sessionStoreRepository.deleteCode(event.getEmail());
            }
            emailSendLogRepository.save(logItem);
        });
    }
}
