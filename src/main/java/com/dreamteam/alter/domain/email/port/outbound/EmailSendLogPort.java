package com.dreamteam.alter.domain.email.port.outbound;

import com.dreamteam.alter.domain.email.entity.EmailSendLog;

import java.util.Optional;

public interface EmailSendLogPort {
    EmailSendLog save(EmailSendLog log);
    Optional<EmailSendLog> findById(Long id);
}
