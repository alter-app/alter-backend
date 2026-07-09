package com.dreamteam.alter.adapter.outbound.chat.persistence;

import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomMemberJpaRepository extends JpaRepository<ChatRoomMember, Long> {
}
