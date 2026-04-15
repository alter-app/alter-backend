package com.dreamteam.alter.application.user.event;

import com.dreamteam.alter.adapter.outbound.user.persistence.SignupSessionCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SignupSessionCleanupListener {

    private static final String KEY_PREFIX = "SIGNUP:PENDING:";
    private static final String CONTACT_INDEX_KEY_PREFIX = "SIGNUP:CONTACT:";

    private final SignupSessionCacheRepository cacheRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSignupCompleted(SignupCompletedEvent event) {
        String sessionKey = KEY_PREFIX + event.getSignupSessionId();
        String contactKey = CONTACT_INDEX_KEY_PREFIX + event.getContact();
        cacheRepository.deleteAll(List.of(sessionKey, contactKey));
    }
}
