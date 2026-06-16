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

@DisplayName("WorkspaceRequest.cancel 테스트")
class WorkspaceRequestTest {

    private WorkspaceRequest pendingRequest() {
        return WorkspaceRequest.create(
            mock(User.class),
            "1234567890",
            "비즈니스",
            "CAFE",
            "01012345678",
            "서울특별시 강남구 역삼동",
            "서울특별시",
            "강남구",
            "역삼동",
            BigDecimal.valueOf(37.5),
            BigDecimal.valueOf(127.0)
        );
    }

    @Test
    @DisplayName("PENDING 상태면 CANCELLED 로 변경된다")
    void cancel_PENDING_정상취소() {
        // given
        WorkspaceRequest request = pendingRequest();

        // when
        request.cancel();

        // then
        assertThat(request.getStatus()).isEqualTo(WorkspaceRequestStatus.CANCELLED);
    }

    @Test
    @DisplayName("ACTIVATED 상태면 CONFLICT 예외가 발생한다")
    void cancel_ACTIVATED_예외발생() {
        // given
        WorkspaceRequest request = pendingRequest();
        request.approve();

        // when & then
        assertThatThrownBy(request::cancel)
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));
    }

    @Test
    @DisplayName("REVOKED 상태면 CONFLICT 예외가 발생한다")
    void cancel_REVOKED_예외발생() {
        // given
        WorkspaceRequest request = pendingRequest();
        request.reject();

        // when & then
        assertThatThrownBy(request::cancel)
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));
    }

    @Test
    @DisplayName("이미 CANCELLED 상태면 CONFLICT 예외가 발생한다")
    void cancel_이미취소됨_예외발생() {
        // given
        WorkspaceRequest request = pendingRequest();
        request.cancel();

        // when & then
        assertThatThrownBy(request::cancel)
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));
    }
}
