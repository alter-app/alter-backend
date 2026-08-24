package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("AbstractChatUseCase 테스트")
class AbstractChatUseCaseTests {

    private final AbstractChatUseCase sut = new AbstractChatUseCase() {};

    @Test
    @DisplayName("ManagerActor의 참여자 id는 ManagerUser.id가 아닌 연관 User.id이고 scope는 MANAGER다")
    void getParticipantId_ManagerActor는_ManagerUser_id가_아닌_연관User_id를_반환한다() {
        // given
        Long managerUserRowId = 999L; // ManagerUser.id (participantId로 쓰이면 안 되는 값)
        Long userId = 10L; // ManagerActor.getUserId()가 반환해야 하는 실제 participantId

        User user = mock(User.class);
        given(user.getId()).willReturn(userId);
        ManagerUser managerUser = mock(ManagerUser.class);
        given(managerUser.getUser()).willReturn(user);
        ManagerActor actor = new ManagerActor(managerUserRowId, managerUser, null);

        // when & then
        assertThat(sut.getParticipantId(actor)).isEqualTo(10L);
        assertThat(sut.getParticipantScope(actor)).isEqualTo(TokenScope.MANAGER);
    }
}
