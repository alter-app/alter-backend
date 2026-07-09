package com.dreamteam.alter.adapter.outbound.chat.persistence;

import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatRoomMemberRepositoryImpl implements ChatRoomMemberRepository {

    private final ChatRoomMemberJpaRepository jpaRepository;

    @Override
    public ChatRoomMember save(ChatRoomMember member) {
        return jpaRepository.save(member);
    }

    @Override
    public List<ChatRoomMember> saveAll(List<ChatRoomMember> members) {
        return jpaRepository.saveAll(members);
    }
}
