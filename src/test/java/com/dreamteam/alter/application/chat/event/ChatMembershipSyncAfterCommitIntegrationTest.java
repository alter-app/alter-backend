package com.dreamteam.alter.application.chat.event;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatSessionRevocationBroadcaster;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

/**
 * ALT-284 최종 리뷰 Blocking 결함 재현/회귀 테스트.
 *
 * ChatMembershipSyncEventListener.onLeft 는 @TransactionalEventListener(AFTER_COMMIT) 이고,
 * 그 콜백 안에서 SyncWorkspaceChatMembership.leave(...) 를 호출한다. leave 는
 * (a) DB에 leftAt 을 반영하고, (b) 세션 강제종료를 위해 ChatSessionRevokeEvent 를 추가로 발행한다.
 *
 * Mockito mock 기반 유닛 테스트는 실제 트랜잭션 경계를 갖지 않으므로 이 문제를 구조적으로 잡지 못한다.
 * 이 테스트는 실제 PlatformTransactionManager + H2 DB로 워커 퇴사 흐름을 커밋시켜,
 * DB 반영 여부와 세션 강제종료 브로드캐스트 호출 여부를 직접 검증한다.
 */
@SpringBootTest
@DisplayName("워커 퇴사(leave) 채팅 멤버십 동기화 - 실제 트랜잭션 커밋 검증")
class ChatMembershipSyncAfterCommitIntegrationTest {

    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @MockitoSpyBean
    private ChatRoomMemberRepository chatRoomMemberRepository;
    @Autowired
    private ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    @MockitoBean
    private ChatSessionRevocationBroadcaster chatSessionRevocationBroadcaster;

    @Test
    @DisplayName("leave 이벤트 커밋 후 chat_room_members.left_at 이 DB에 반영되고, 세션 강제종료 브로드캐스트가 실제로 호출된다")
    void leave_커밋후_DB반영과_세션강제종료가_실제로_일어난다() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        Long workspaceId = 9001L;
        Long memberId = 9002L;
        TokenScope scope = TokenScope.APP;

        // given: 그룹방 + 활성 멤버를 별도 트랜잭션에서 미리 커밋해둔다
        Long roomId = tx.execute(status -> {
            ChatRoom room = chatRoomRepository.save(ChatRoom.createGroup(workspaceId));
            chatRoomMemberRepository.save(ChatRoomMember.create(room.getId(), memberId, scope));
            return room.getId();
        });

        // when: 실제 프로덕션 경로처럼 별도 트랜잭션 안에서 ChatMembershipLeftEvent 를 발행하고 커밋한다.
        // -> 커밋 시점에 ChatMembershipSyncEventListener.onLeft(AFTER_COMMIT) 가 실행되어야 한다.
        tx.executeWithoutResult(status -> eventPublisher.publishEvent(new ChatMembershipLeftEvent(workspaceId, memberId, scope)));

        // then (a): DB에 leftAt 이 실제로 반영됐는지 - 새 트랜잭션(=새 persistence context)에서 재조회
        Boolean stillActive = tx.execute(status ->
            chatRoomMemberQueryRepository.findByRoomAndMember(roomId, memberId, scope)
                .map(ChatRoomMember::isActive)
                .orElseThrow());
        assertThat(stillActive)
            .as("워커 퇴사 후 chat_room_members.left_at 이 DB에 반영되어야 한다")
            .isFalse();

        // then (b): leave() 안에서 발행한 ChatSessionRevokeEvent 의 AFTER_COMMIT 리스너가 호출되어
        // ChatSessionRevocationBroadcaster.revoke 가 실제로 실행됐는지
        then(chatSessionRevocationBroadcaster).should().revoke(scope, memberId, roomId);
    }

    @Test
    @DisplayName("leave 도중 예외가 나도 onLeft 의 try/catch 안에서 삼켜져 바깥 트랜잭션 커밋까지 예외 없이 끝난다")
    void leave_도중_예외가_나도_바깥_트랜잭션_커밋에_새지_않는다() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        Long workspaceId = 9101L;
        Long memberId = 9102L;
        TokenScope scope = TokenScope.APP;

        // given: 그룹방 + 활성 멤버를 미리 커밋해둔다 (아직 spy 스텁 전이라 실제 저장이 성공한다)
        Long roomId = tx.execute(status -> {
            ChatRoom room = chatRoomRepository.save(ChatRoom.createGroup(workspaceId));
            chatRoomMemberRepository.save(ChatRoomMember.create(room.getId(), memberId, scope));
            return room.getId();
        });

        // sync.leave() 가 member.leave() 이후 DB 저장 시 런타임 예외를 던지도록 강제한다.
        // (REQUIRES_NEW 트랜잭션 커밋 경계가 onLeft 의 try/catch 안쪽에 있어야만 이 예외가 여기서 잡힌다)
        willThrow(new RuntimeException("강제 DB 오류")).given(chatRoomMemberRepository).save(any());

        // when & then: 워커 퇴사 이벤트를 발행하고 커밋하는 바깥 트랜잭션 자체는 예외 없이 끝나야 한다.
        assertThatCode(() ->
            tx.executeWithoutResult(status -> eventPublisher.publishEvent(new ChatMembershipLeftEvent(workspaceId, memberId, scope)))
        ).as("leave() 내부 예외가 onLeft 의 try/catch 를 벗어나 바깥 커밋까지 새면 안 된다")
            .doesNotThrowAnyException();

        // then: leave() 의 REQUIRES_NEW 트랜잭션은 롤백됐으므로 세션 강제종료도 발생하지 않는다
        then(chatSessionRevocationBroadcaster).shouldHaveNoInteractions();
    }
}
