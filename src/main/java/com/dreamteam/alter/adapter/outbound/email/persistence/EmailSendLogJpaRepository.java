package com.dreamteam.alter.adapter.outbound.email.persistence;

import com.dreamteam.alter.domain.email.entity.EmailSendLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailSendLogJpaRepository extends JpaRepository<EmailSendLog, Long> {
}
