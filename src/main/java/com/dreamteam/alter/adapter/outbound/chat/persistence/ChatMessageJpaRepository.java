package com.dreamteam.alter.adapter.outbound.chat.persistence;

import com.dreamteam.alter.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessage, Long> {
}
