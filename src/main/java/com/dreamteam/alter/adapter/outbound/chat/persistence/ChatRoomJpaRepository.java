package com.dreamteam.alter.adapter.outbound.chat.persistence;

import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoom, Long> {
}
