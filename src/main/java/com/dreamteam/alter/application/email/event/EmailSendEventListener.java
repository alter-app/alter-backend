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

    private static final int MAX_RETRY = 2;
    private static final long RETRY_DELAY_MS = 1000L;

    private final EmailSendLogRepository emailSendLogRepository;
    private final EmailClient emailClient;
    private final EmailVerificationSessionStoreRepository sessionStoreRepository;

    @Async("emailTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmailSendEvent(EmailSendEvent event) {
        Long logId = event.getLogId();

        emailSendLogRepository.findById(logId).ifPresent(logItem -> {
            Exception lastException = null;

            for (int attempt = 0; attempt <= MAX_RETRY; attempt++) {
                try {
                    emailClient.sendVerificationCode(event.getEmail(), event.getCode());
                    logItem.markSent();
                    lastException = null;
                    break;
                } catch (Exception e) {
                    lastException = e;
                    if (attempt < MAX_RETRY) {
                        try {
                            Thread.sleep(RETRY_DELAY_MS * (attempt + 1));
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }

            if (lastException != null) {
                log.error("{}회 재시도 후 이메일 발송 실패 to: {}", MAX_RETRY + 1, event.getEmail(), lastException);
                logItem.markFailed();
                sessionStoreRepository.deleteCode(event.getEmail());
            }

            emailSendLogRepository.save(logItem);
        });
    }
}
