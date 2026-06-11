package com.dreamteam.alter.domain.workspace.entity;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.type.WorkspaceRequestStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@DisplayName("WorkspaceRequest 테스트")
class WorkspaceRequestTest {

    private WorkspaceRequest createPendingRequest() {
        return WorkspaceRequest.create(
            mock(User.class),
            "123-45-67890",
            "테스트업장",
            "홍길동",
            "음식점",
            "02-1234-5678",
            "서울특별시 구로구 고척동 123",
            "서울특별시",
            "구로구",
            "고척동",
            new BigDecimal("37.5665"),
            new BigDecimal("126.9780")
        );
    }

    @Test
    @DisplayName("PENDING 상태의 요청은 취소되어 CANCELED 가 된다")
    void cancel_fromPending() {
        // given
        WorkspaceRequest request = createPendingRequest();

        // when
        request.cancel();

        // then
        assertThat(request.getStatus()).isEqualTo(WorkspaceRequestStatus.CANCELED);
    }

    @Test
    @DisplayName("REVOKED(반려) 상태의 요청은 취소되어 CANCELED 가 된다")
    void cancel_fromRevoked() {
        // given
        WorkspaceRequest request = createPendingRequest();
        request.reject();

        // when
        request.cancel();

        // then
        assertThat(request.getStatus()).isEqualTo(WorkspaceRequestStatus.CANCELED);
    }

    @Test
    @DisplayName("ACTIVATED(승인) 상태의 요청을 취소하면 CONFLICT 예외 발생")
    void cancel_fromActivated_throwsConflict() {
        // given
        WorkspaceRequest request = createPendingRequest();
        request.approve();

        // when & then
        assertThatThrownBy(request::cancel)
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));
    }

    @Test
    @DisplayName("이미 CANCELED 상태의 요청을 다시 취소하면 CONFLICT 예외 발생")
    void cancel_fromCanceled_throwsConflict() {
        // given
        WorkspaceRequest request = createPendingRequest();
        request.cancel();

        // when & then
        assertThatThrownBy(request::cancel)
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));
    }
}
