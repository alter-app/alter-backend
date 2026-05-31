package com.dreamteam.alter.domain.workspace.entity;

import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestTargetStatus;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("SubstituteRequest 테스트")
class SubstituteRequestTest {

    @Test
    @DisplayName("유일한 pending target이 취소되면 부모 요청도 취소된다")
    void cancelPendingTarget_유일한타깃_부모요청취소() {
        // given
        SubstituteRequest request = SubstituteRequest.create(
            mock(WorkspaceShift.class),
            1L,
            SubstituteRequestType.SPECIFIC,
            "사유"
        );
        SubstituteRequestTarget target = SubstituteRequestTarget.create(request, 10L);
        request.getTargets().add(target);

        // when
        request.cancelPendingTargetAndCancelIfNoPendingTargets(10L);

        // then
        assertThat(target.getStatus()).isEqualTo(SubstituteRequestTargetStatus.CANCELLED);
        assertThat(request.getStatus()).isEqualTo(SubstituteRequestStatus.CANCELLED);
    }

    @Test
    @DisplayName("다른 pending target이 남아 있으면 부모 요청은 대기 상태를 유지한다")
    void cancelPendingTarget_다른Pending존재_부모요청유지() {
        // given
        SubstituteRequest request = SubstituteRequest.create(
            mock(WorkspaceShift.class),
            1L,
            SubstituteRequestType.ALL,
            "사유"
        );
        SubstituteRequestTarget resignedTarget = SubstituteRequestTarget.create(request, 10L);
        SubstituteRequestTarget remainingTarget = SubstituteRequestTarget.create(request, 20L);
        request.getTargets().add(resignedTarget);
        request.getTargets().add(remainingTarget);

        // when
        request.cancelPendingTargetAndCancelIfNoPendingTargets(10L);

        // then
        assertThat(resignedTarget.getStatus()).isEqualTo(SubstituteRequestTargetStatus.CANCELLED);
        assertThat(remainingTarget.getStatus()).isEqualTo(SubstituteRequestTargetStatus.PENDING);
        assertThat(request.getStatus()).isEqualTo(SubstituteRequestStatus.PENDING);
    }

    @Test
    @DisplayName("마지막 pending target이 취소되면 부모 요청도 취소된다")
    void cancelPendingTarget_마지막Pending_부모요청취소() {
        // given
        SubstituteRequest request = SubstituteRequest.create(
            mock(WorkspaceShift.class),
            1L,
            SubstituteRequestType.ALL,
            "사유"
        );
        SubstituteRequestTarget rejectedTarget = SubstituteRequestTarget.create(request, 10L);
        rejectedTarget.reject("불가");
        SubstituteRequestTarget resignedTarget = SubstituteRequestTarget.create(request, 20L);
        request.getTargets().add(rejectedTarget);
        request.getTargets().add(resignedTarget);

        // when
        request.cancelPendingTargetAndCancelIfNoPendingTargets(20L);

        // then
        assertThat(rejectedTarget.getStatus()).isEqualTo(SubstituteRequestTargetStatus.REJECTED);
        assertThat(resignedTarget.getStatus()).isEqualTo(SubstituteRequestTargetStatus.CANCELLED);
        assertThat(request.getStatus()).isEqualTo(SubstituteRequestStatus.CANCELLED);
    }
}
