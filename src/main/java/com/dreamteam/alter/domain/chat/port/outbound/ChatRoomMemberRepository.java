package com.dreamteam.alter.domain.chat.port.outbound;

import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import java.util.List;

public interface ChatRoomMemberRepository {
    ChatRoomMember save(ChatRoomMember member);
    List<ChatRoomMember> saveAll(List<ChatRoomMember> members);
}
