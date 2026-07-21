package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 업장 단톡방(GROUP) 생성 전용 컴포넌트.
 * find-후-save를 독립 트랜잭션(REQUIRES_NEW)에서 수행하여, 동시 생성 경쟁으로
 * V7 유니크 인덱스 위반이 발생해도 그 롤백이 호출자 트랜잭션을 오염시키지 않도록 한다.
 * (같은 트랜잭션에서 유니크 위반 후 재조회하면 rollback-only 상태라 실패하기 때문에 분리한다.)
 */
@Component
@RequiredArgsConstructor
public class GroupChatRoomProvider {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomQueryRepository chatRoomQueryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long getOrCreate(Long workspaceId) {
        return chatRoomQueryRepository.findGroupRoomByWorkspaceId(workspaceId)
            .map(ChatRoom::getId)
            .orElseGet(() -> chatRoomRepository.save(ChatRoom.createGroup(workspaceId)).getId());
    }
}
