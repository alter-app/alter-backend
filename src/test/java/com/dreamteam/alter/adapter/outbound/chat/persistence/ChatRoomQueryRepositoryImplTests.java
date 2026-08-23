package com.dreamteam.alter.adapter.outbound.chat.persistence;

import com.dreamteam.alter.adapter.inbound.common.dto.ChatRoomCursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatRoomListWithOpponentResponse;
import com.dreamteam.alter.adapter.outbound.file.persistence.FileRepositoryImpl;
import com.dreamteam.alter.adapter.outbound.user.persistence.UserRepositoryImpl;
import com.dreamteam.alter.common.config.QueryDslConfig;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.type.BucketType;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.type.UserGender;
import com.dreamteam.alter.domain.user.type.UserStatus;
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
    ChatRoomMemberRepositoryImpl.class,
    UserRepositoryImpl.class,
    FileRepositoryImpl.class
})
class ChatRoomQueryRepositoryImplTests {

    @Autowired
    private ChatRoomRepositoryImpl chatRoomRepository;

    @Autowired
    private ChatRoomQueryRepositoryImpl chatRoomQueryRepository;

    @Autowired
    private ChatRoomMemberRepositoryImpl chatRoomMemberRepository;

    @Autowired
    private UserRepositoryImpl userRepository;

    @Autowired
    private FileRepositoryImpl fileRepository;

    private CursorPageRequest<ChatRoomCursorDto> firstPage() {
        return CursorPageRequest.of(null, 20);
    }

    private User saveUser(UserStatus status) {
        User user = User.create(
            "01000000000", "encoded", "김알바", "nickname" + System.nanoTime(),
            UserGender.GENDER_MALE, "19990101", "user" + System.nanoTime() + "@example.com"
        );
        if (status != UserStatus.ACTIVE) {
            user.updateStatus(status);
        }
        return userRepository.save(user);
    }

    private File saveAttachedProfileFile(Long targetUserId, String fileUrl) {
        File file = File.create(
            FileTargetType.USER_PROFILE, "profile.png", "stored/profile" + System.nanoTime() + ".png",
            fileUrl, "image/png", 1024L, BucketType.PUBLIC, targetUserId
        );
        file.attach(String.valueOf(targetUserId));
        return fileRepository.save(file);
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

    @Test
    void getChatRoomListWithOpponent_상대ATTACHED프로필파일_2건이어도_방_1행과_가장최신파일URL만_반환() {
        User opponent = saveUser(UserStatus.ACTIVE);
        File oldestFile = saveAttachedProfileFile(opponent.getId(), "https://cdn.example.com/oldest.png");
        File newestFile = saveAttachedProfileFile(opponent.getId(), "https://cdn.example.com/newest.png");

        ChatRoom directRoom = chatRoomRepository.save(
            ChatRoom.create(82L, TokenScope.APP, opponent.getId(), TokenScope.APP));
        chatRoomMemberRepository.save(ChatRoomMember.create(directRoom.getId(), 82L, TokenScope.APP));
        chatRoomMemberRepository.save(ChatRoomMember.create(directRoom.getId(), opponent.getId(), TokenScope.APP));

        List<ChatRoomListWithOpponentResponse> result =
            chatRoomQueryRepository.getChatRoomListWithOpponent(82L, TokenScope.APP, firstPage());
        long totalCount = chatRoomQueryRepository.countChatRoomsByParticipant(82L, TokenScope.APP);

        List<ChatRoomListWithOpponentResponse> matched = result.stream()
            .filter(r -> r.getId().equals(directRoom.getId()))
            .toList();

        assertThat(matched).hasSize(1);
        assertThat(matched.get(0).getOpponentName()).isEqualTo(opponent.getName());
        assertThat(matched.get(0).getOpponentProfileImageUrl()).isEqualTo(newestFile.getFileUrl());
        assertThat(matched.get(0).getOpponentProfileImageUrl()).isNotEqualTo(oldestFile.getFileUrl());
        assertThat(totalCount).isEqualTo(result.size());
    }

    @Test
    void getChatRoomListWithOpponent_비활성상대는_이름과_프로필URL_모두_null() {
        User opponent = saveUser(UserStatus.SUSPENDED);
        saveAttachedProfileFile(opponent.getId(), "https://cdn.example.com/profile.png");

        ChatRoom directRoom = chatRoomRepository.save(
            ChatRoom.create(81L, TokenScope.APP, opponent.getId(), TokenScope.APP));
        chatRoomMemberRepository.save(ChatRoomMember.create(directRoom.getId(), 81L, TokenScope.APP));
        chatRoomMemberRepository.save(ChatRoomMember.create(directRoom.getId(), opponent.getId(), TokenScope.APP));

        List<ChatRoomListWithOpponentResponse> result =
            chatRoomQueryRepository.getChatRoomListWithOpponent(81L, TokenScope.APP, firstPage());

        ChatRoomListWithOpponentResponse response = result.stream()
            .filter(r -> r.getId().equals(directRoom.getId()))
            .findFirst()
            .orElseThrow();

        assertThat(response.getOpponentName()).isNull();
        assertThat(response.getOpponentProfileImageUrl()).isNull();
    }

    @Test
    void getChatRoomListWithOpponent_memberCount는_활성멤버수와_일치하고_나간멤버는_제외() {
        ChatRoom groupRoom = chatRoomRepository.save(ChatRoom.createGroup(104L));
        chatRoomMemberRepository.save(ChatRoomMember.create(groupRoom.getId(), 90L, TokenScope.APP));
        chatRoomMemberRepository.save(ChatRoomMember.create(groupRoom.getId(), 91L, TokenScope.APP));
        ChatRoomMember leftMember = chatRoomMemberRepository.save(
            ChatRoomMember.create(groupRoom.getId(), 92L, TokenScope.APP));
        leftMember.leave();
        chatRoomMemberRepository.save(leftMember);

        List<ChatRoomListWithOpponentResponse> result =
            chatRoomQueryRepository.getChatRoomListWithOpponent(90L, TokenScope.APP, firstPage());

        ChatRoomListWithOpponentResponse response = result.stream()
            .filter(r -> r.getId().equals(groupRoom.getId()))
            .findFirst()
            .orElseThrow();

        assertThat(response.getMemberCount()).isEqualTo(2L);
    }

    // 매니저를 조회 주체로 한 케이스: chat_room_members.member_id는 manager_users.user_id
    // (= ManagerActor.getUserId(), User.id)여야 하고 ManagerUser.id가 아니다.

    @Test
    void findByIdAndParticipant_매니저user_id로_조회되고_scope다르면_조회안됨() {
        User managerUnderlyingUser = saveUser(UserStatus.ACTIVE);
        ChatRoom groupRoom = chatRoomRepository.save(ChatRoom.createGroup(105L));
        chatRoomMemberRepository.save(
            ChatRoomMember.create(groupRoom.getId(), managerUnderlyingUser.getId(), TokenScope.MANAGER));

        Optional<ChatRoom> found = chatRoomQueryRepository.findByIdAndParticipant(
            groupRoom.getId(), managerUnderlyingUser.getId(), TokenScope.MANAGER);
        Optional<ChatRoom> notFoundWrongScope = chatRoomQueryRepository.findByIdAndParticipant(
            groupRoom.getId(), managerUnderlyingUser.getId(), TokenScope.APP);

        assertThat(found).isPresent();
        assertThat(notFoundWrongScope).isEmpty();
    }

    @Test
    void getChatRoomListWithOpponent_매니저participant_기준_목록에_포함() {
        User managerUnderlyingUser = saveUser(UserStatus.ACTIVE);
        User opponent = saveUser(UserStatus.ACTIVE);
        ChatRoom directRoom = chatRoomRepository.save(
            ChatRoom.create(managerUnderlyingUser.getId(), TokenScope.MANAGER, opponent.getId(), TokenScope.APP));
        chatRoomMemberRepository.save(
            ChatRoomMember.create(directRoom.getId(), managerUnderlyingUser.getId(), TokenScope.MANAGER));
        chatRoomMemberRepository.save(
            ChatRoomMember.create(directRoom.getId(), opponent.getId(), TokenScope.APP));

        List<ChatRoomListWithOpponentResponse> result = chatRoomQueryRepository.getChatRoomListWithOpponent(
            managerUnderlyingUser.getId(), TokenScope.MANAGER, firstPage());

        ChatRoomListWithOpponentResponse response = result.stream()
            .filter(r -> r.getId().equals(directRoom.getId()))
            .findFirst()
            .orElseThrow();

        assertThat(response.getOpponentId()).isEqualTo(opponent.getId());
        assertThat(response.getOpponentScope()).isEqualTo(TokenScope.APP);
        assertThat(response.getOpponentName()).isEqualTo(opponent.getName());
    }
}
