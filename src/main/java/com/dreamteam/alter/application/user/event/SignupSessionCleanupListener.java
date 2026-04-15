package com.dreamteam.alter.application.user.event;

import com.dreamteam.alter.adapter.outbound.user.persistence.SignupSessionCacheRepository;
import com.dreamteam.alter.common.constants.SignupSessionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SignupSessionCleanupListener {

    private final SignupSessionCacheRepository cacheRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSignupCompleted(SignupCompletedEvent event) {
        String sessionKey = SignupSessionConstants.Session.KEY_PREFIX + event.getSignupSessionId();
        String contactKey = SignupSessionConstants.Session.CONTACT_INDEX_KEY_PREFIX + event.getContact();
        cacheRepository.deleteAll(List.of(sessionKey, contactKey));
    }
}
