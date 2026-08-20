package com.dreamteam.alter.adapter.outbound.chat.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.ChatRoomCursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatRoomListWithOpponentResponse;
import com.dreamteam.alter.common.config.QueryDslConfig;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
    QueryDslConfig.class,
    ChatRoomRepositoryImpl.class,
    ChatRoomQueryRepositoryImpl.class,
    ChatRoomMemberRepositoryImpl.class
})
class ChatRoomQueryRepositoryImplTests {

    @Autowired
    private ChatRoomRepositoryImpl chatRoomRepository;

    @Autowired
    private ChatRoomQueryRepositoryImpl chatRoomQueryRepository;

    @Autowired
    private ChatRoomMemberRepositoryImpl chatRoomMemberRepository;

    private CursorPageRequest<ChatRoomCursorDto> firstPage() {
        return CursorPageRequest.of(null, 20);
    }

    @Test
    void getChatRoomListWithOpponent_GROUP_활성멤버는_목록에_포함() {
        ChatRoom groupRoom = chatRoomRepository.save(ChatRoom.createGroup(100L));
        chatRoomMemberRepository.save(ChatRoomMember.create(groupRoom.getId(), 10L, TokenScope.APP));

        List<ChatRoomListWithOpponentResponse> result =
            chatRoomQueryRepository.getChatRoomListWithOpponent(10L, TokenScope.APP, firstPage());

        assertThat(result).extracting(ChatRoomListWithOpponentResponse::getId)
            .contains(groupRoom.getId());
    }

    @Test
    void getChatRoomListWithOpponent_GROUP_나간멤버는_목록에서_제외() {
        ChatRoom groupRoom = chatRoomRepository.save(ChatRoom.createGroup(101L));
        ChatRoomMember member = chatRoomMemberRepository.save(
            ChatRoomMember.create(groupRoom.getId(), 20L, TokenScope.APP));
        member.leave();
        chatRoomMemberRepository.save(member);

        List<ChatRoomListWithOpponentResponse> result =
            chatRoomQueryRepository.getChatRoomListWithOpponent(20L, TokenScope.APP, firstPage());

        assertThat(result).extracting(ChatRoomListWithOpponentResponse::getId)
            .doesNotContain(groupRoom.getId());
    }

    @Test
    void findByIdAndParticipant_GROUP_활성멤버는_조회되고_비멤버는_조회안됨() {
        ChatRoom groupRoom = chatRoomRepository.save(ChatRoom.createGroup(102L));
        chatRoomMemberRepository.save(ChatRoomMember.create(groupRoom.getId(), 30L, TokenScope.APP));

        Optional<ChatRoom> found = chatRoomQueryRepository.findByIdAndParticipant(
            groupRoom.getId(), 30L, TokenScope.APP);
        Optional<ChatRoom> notFound = chatRoomQueryRepository.findByIdAndParticipant(
            groupRoom.getId(), 99L, TokenScope.APP);

        assertThat(found).isPresent();
        assertThat(notFound).isEmpty();
    }

    @Test
    void getChatRoomListWithOpponent_DIRECT_멤버행_기준으로_목록에_포함() {
        ChatRoom directRoom = chatRoomRepository.save(
            ChatRoom.create(40L, TokenScope.APP, 50L, TokenScope.MANAGER));
        chatRoomMemberRepository.save(ChatRoomMember.create(directRoom.getId(), 40L, TokenScope.APP));
        chatRoomMemberRepository.save(ChatRoomMember.create(directRoom.getId(), 50L, TokenScope.MANAGER));

        List<ChatRoomListWithOpponentResponse> result =
            chatRoomQueryRepository.getChatRoomListWithOpponent(40L, TokenScope.APP, firstPage());

        assertThat(result).extracting(ChatRoomListWithOpponentResponse::getId)
            .contains(directRoom.getId());
    }

    @Test
    void getChatRoomListWithOpponent_멤버행없으면_participant컬럼있어도_목록에서_제외() {
        ChatRoom directRoom = chatRoomRepository.save(
            ChatRoom.create(41L, TokenScope.APP, 51L, TokenScope.MANAGER));
        // ChatRoomMember 행을 생성하지 않음: participant 컬럼만으로는 목록에 포함되지 않아야 한다

        List<ChatRoomListWithOpponentResponse> result =
            chatRoomQueryRepository.getChatRoomListWithOpponent(41L, TokenScope.APP, firstPage());

        assertThat(result).extracting(ChatRoomListWithOpponentResponse::getId)
            .doesNotContain(directRoom.getId());
    }

    @Test
    void countChatRoomsByParticipant_목록건수와_일치() {
        ChatRoom groupRoom = chatRoomRepository.save(ChatRoom.createGroup(103L));
        chatRoomMemberRepository.save(ChatRoomMember.create(groupRoom.getId(), 60L, TokenScope.APP));
        chatRoomRepository.save(ChatRoom.create(60L, TokenScope.APP, 70L, TokenScope.MANAGER));

        long count = chatRoomQueryRepository.countChatRoomsByParticipant(60L, TokenScope.APP);
        List<ChatRoomListWithOpponentResponse> list =
            chatRoomQueryRepository.getChatRoomListWithOpponent(60L, TokenScope.APP, firstPage());

        assertThat(count).isEqualTo(list.size());
    }
}
