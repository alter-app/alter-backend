package com.dreamteam.alter.domain.workspace.entity;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WorkspaceImage 엔티티 테스트")
class WorkspaceImageTest {

    @Test
    @DisplayName("정상 sortOrder로 생성된다")
    void create_정상() {
        // when
        WorkspaceImage image = WorkspaceImage.create(null, "file-1", 0);

        // then
        assertThat(image.getFileId()).isEqualTo("file-1");
        assertThat(image.getSortOrder()).isZero();
    }

    @Test
    @DisplayName("음수 sortOrder로 생성하면 ILLEGAL_ARGUMENT 예외가 발생한다")
    void create_음수sortOrder_예외() {
        // when & then
        assertThatThrownBy(() -> WorkspaceImage.create(null, "file-1", -1))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.ILLEGAL_ARGUMENT));
    }

    @Test
    @DisplayName("updateSortOrder는 0 이상 값을 허용한다")
    void updateSortOrder_정상() {
        // given
        WorkspaceImage image = WorkspaceImage.create(null, "file-1", 0);

        // when & then
        assertThatCode(() -> image.updateSortOrder(3)).doesNotThrowAnyException();
        assertThat(image.getSortOrder()).isEqualTo(3);
    }

    @Test
    @DisplayName("updateSortOrder에 음수를 주면 ILLEGAL_ARGUMENT 예외가 발생한다")
    void updateSortOrder_음수_예외() {
        // given
        WorkspaceImage image = WorkspaceImage.create(null, "file-1", 0);

        // when & then
        assertThatThrownBy(() -> image.updateSortOrder(-1))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.ILLEGAL_ARGUMENT));
    }
}
