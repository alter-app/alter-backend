package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.application.chat.event.ChatMembershipJoinedEvent;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddWorkerToWorkspace 테스트")
class AddWorkerToWorkspaceTests {

    @Mock
    private WorkspaceWorkerRepository workspaceWorkerRepository;

    @Mock
    private WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AddWorkerToWorkspace addWorkerToWorkspace;

    @Captor
    private ArgumentCaptor<ChatMembershipJoinedEvent> eventCaptor;

    @Test
    @DisplayName("신규 근무자 추가 시 업장 단톡 join 이벤트를 발행한다")
    void execute_신규추가_단톡join이벤트발행() {
        // given
        Workspace workspace = mock(Workspace.class);
        given(workspace.getId()).willReturn(1L);
        User user = mock(User.class);
        given(user.getId()).willReturn(2L);

        given(workspaceWorkerQueryRepository.findActiveWorkerByWorkspaceAndUser(workspace, user))
            .willReturn(Optional.empty());

        // when
        addWorkerToWorkspace.execute(workspace, user);

        // then
        then(eventPublisher).should().publishEvent(eventCaptor.capture());
        ChatMembershipJoinedEvent event = eventCaptor.getValue();
        assertThat(event.getWorkspaceId()).isEqualTo(1L);
        assertThat(event.getMemberId()).isEqualTo(2L);
        assertThat(event.getScope()).isEqualTo(TokenScope.APP);
    }

    @Test
    @DisplayName("이미 근무중인 근무자면 예외를 던지고 단톡 join 이벤트를 발행하지 않는다")
    void execute_이미근무중_단톡join이벤트미발행() {
        // given
        Workspace workspace = mock(Workspace.class);
        User user = mock(User.class);
        WorkspaceWorker existingWorker = mock(WorkspaceWorker.class);

        given(workspaceWorkerQueryRepository.findActiveWorkerByWorkspaceAndUser(workspace, user))
            .willReturn(Optional.of(existingWorker));

        // when & then
        assertThatThrownBy(() -> addWorkerToWorkspace.execute(workspace, user))
            .isInstanceOf(CustomException.class);

        then(eventPublisher).should(never()).publishEvent(any());
    }
}
