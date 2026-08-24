package com.dreamteam.alter.adapter.outbound.chat.persistence;

import com.dreamteam.alter.common.config.QueryDslConfig;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
    QueryDslConfig.class,
    ChatRoomMemberRepositoryImpl.class,
    ChatRoomMemberQueryRepositoryImpl.class
})
class ChatRoomMemberRepositoryImplTests {

    @Autowired
    private ChatRoomMemberRepositoryImpl repository;

    @Autowired
    private ChatRoomMemberQueryRepositoryImpl queryRepository;

    @Test
    void findActiveByRoom_활성_멤버만_반환() {
        repository.save(ChatRoomMember.create(1L, 10L, TokenScope.APP));
        ChatRoomMember b = repository.save(ChatRoomMember.create(1L, 20L, TokenScope.MANAGER));
        b.leave();
        repository.save(b);

        List<ChatRoomMember> active = queryRepository.findActiveByRoom(1L);

        assertThat(active).hasSize(1);
        assertThat(active.get(0).getMemberId()).isEqualTo(10L);
    }

    @Test
    void findByRoomAndMember_존재하면_조회됨() {
        repository.save(ChatRoomMember.create(2L, 30L, TokenScope.APP));

        Optional<ChatRoomMember> found = queryRepository.findByRoomAndMember(2L, 30L, TokenScope.APP);

        assertThat(found).isPresent();
        assertThat(found.get().getChatRoomId()).isEqualTo(2L);
    }

    @Test
    void existsActive_leave하면_false() {
        ChatRoomMember member = repository.save(ChatRoomMember.create(3L, 40L, TokenScope.APP));

        assertThat(queryRepository.existsActive(3L, 40L, TokenScope.APP)).isTrue();

        member.leave();
        repository.save(member);

        assertThat(queryRepository.existsActive(3L, 40L, TokenScope.APP)).isFalse();
    }

    @Test
    void countActiveByRoomIds_방2개_나간멤버제외집계_멤버없는방은키부재() {
        // Room 1: 2 active members, 1 left member
        repository.save(ChatRoomMember.create(4L, 50L, TokenScope.APP));
        repository.save(ChatRoomMember.create(4L, 60L, TokenScope.MANAGER));
        ChatRoomMember leftMember = repository.save(ChatRoomMember.create(4L, 70L, TokenScope.APP));
        leftMember.leave();
        repository.save(leftMember);

        // Room 2: no members
        Map<Long, Long> counts = queryRepository.countActiveByRoomIds(List.of(4L, 5L));

        assertThat(counts).hasSize(1);
        assertThat(counts).containsEntry(4L, 2L);
        assertThat(counts).doesNotContainKey(5L);
    }
}
