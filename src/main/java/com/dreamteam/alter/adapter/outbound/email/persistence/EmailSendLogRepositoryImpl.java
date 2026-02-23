package com.dreamteam.alter.adapter.outbound.email.persistence;

import com.dreamteam.alter.domain.email.entity.EmailSendLog;
import com.dreamteam.alter.domain.email.port.outbound.EmailSendLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmailSendLogRepositoryImpl implements EmailSendLogRepository {

    private final EmailSendLogJpaRepository jpaRepository;

    @Override
    public EmailSendLog save(EmailSendLog log) {
        return jpaRepository.save(log);
    }

    @Override
    public Optional<EmailSendLog> findById(Long id) {
        return jpaRepository.findById(id);
    }
}
