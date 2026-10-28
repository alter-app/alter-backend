package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingListFilterDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingListResponse;
import com.dreamteam.alter.common.util.CursorUtil;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPostingsWithCursor - 일반 공고 목록 커서 조회")
class GetPostingsWithCursorTests {
    @Mock PostingQueryRepository postingQueryRepository;
    @Spy ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    @InjectMocks GetPostingsWithCursor useCase;

    private PostingListResponse row(Long id) {
        PostingListResponse row = mock(PostingListResponse.class);
        Workspace workspace = mock(Workspace.class);
        lenient().when(workspace.getBusinessType()).thenReturn(BusinessType.create("카페", "설명"));
        lenient().when(row.getWorkspace()).thenReturn(workspace);
        lenient().when(row.getSchedules()).thenReturn(List.of());
        lenient().when(row.getId()).thenReturn(id);
        lenient().when(row.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 10, 3, 12, 0).minusDays(id));
        return row;
    }

    private AppActor actor() {
        AppActor actor = mock(AppActor.class);
        lenient().when(actor.getUser()).thenReturn(mock(User.class));
        return actor;
    }

    @Test
    @DisplayName("마지막 페이지가 페이지 크기보다 작으면 커서를 내려주지 않는다")
    void lastPageShorterThanPageSizeHasNoCursor() {
        // given
        given(postingQueryRepository.getCountOfPostings(any())).willReturn(2L);
        List<PostingListResponse> rows = List.of(row(1L), row(2L));
        given(postingQueryRepository.getPostingsWithCursor(any(), any(), any()))
            .willReturn(rows);

        // when
        var result = useCase.execute(new CursorPageRequestDto(null, 10), new PostingListFilterDto(), actor());

        // then
        assertThat(result.page().cursor()).isNull();
        assertThat(result.page().pageSize()).isEqualTo(10);
        assertThat(result.page().totalCount()).isEqualTo(2);
        assertThat(result.data()).hasSize(2);
    }

    @Test
    @DisplayName("마지막 페이지가 페이지 크기와 같아도 다음 행이 없으면 커서를 내려주지 않는다")
    void lastPageExactlyPageSizeHasNoCursor() {
        // given
        given(postingQueryRepository.getCountOfPostings(any())).willReturn(2L);
        List<PostingListResponse> rows = List.of(row(1L), row(2L));
        given(postingQueryRepository.getPostingsWithCursor(any(), any(), any()))
            .willReturn(rows);

        // when
        var result = useCase.execute(new CursorPageRequestDto(null, 2), new PostingListFilterDto(), actor());

        // then
        assertThat(result.page().cursor()).isNull();
        assertThat(result.data()).hasSize(2);
    }

    @Test
    @DisplayName("다음 페이지가 있으면 추가로 조회한 행은 빼고, 커서는 마지막으로 반환한 행을 가리킨다")
    void middlePageDropsExtraRowAndPointsCursorAtLastReturnedRow() {
        // given
        given(postingQueryRepository.getCountOfPostings(any())).willReturn(3L);
        List<PostingListResponse> rows = List.of(row(1L), row(2L), row(3L));
        given(postingQueryRepository.getPostingsWithCursor(any(), any(), any()))
            .willReturn(rows);

        // when
        var result = useCase.execute(new CursorPageRequestDto(null, 2), new PostingListFilterDto(), actor());

        // then
        assertThat(result.data()).extracting(dto -> dto.getId()).containsExactly(1L, 2L);
        assertThat(result.page().totalCount()).isEqualTo(3);
        CursorDto cursor = CursorUtil.decodeCursor(result.page().cursor(), CursorDto.class, objectMapper);
        assertThat(cursor.getId()).isEqualTo(2L);
        assertThat(cursor.getCreatedAt()).isEqualTo(rows.get(1).getCreatedAt());
    }

    @Test
    @DisplayName("다음 페이지 여부를 알기 위해 페이지 크기보다 한 행 더 조회한다")
    void repositoryIsAskedForOneMoreRowThanPageSize() {
        // given
        given(postingQueryRepository.getCountOfPostings(any())).willReturn(1L);
        List<PostingListResponse> rows = List.of(row(1L));
        given(postingQueryRepository.getPostingsWithCursor(any(), any(), any()))
            .willReturn(rows);

        // when
        useCase.execute(new CursorPageRequestDto(null, 5), new PostingListFilterDto(), actor());

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<CursorPageRequest<CursorDto>> captor = ArgumentCaptor.forClass(CursorPageRequest.class);
        then(postingQueryRepository).should().getPostingsWithCursor(captor.capture(), any(), any());
        assertThat(captor.getValue().pageSize()).isEqualTo(6);
    }

    @Test
    @DisplayName("조건에 맞는 공고가 없으면 커서 없이 빈 목록을 반환한다")
    void noMatchingPostingsReturnsEmptyWithoutCursor() {
        // given
        given(postingQueryRepository.getCountOfPostings(any())).willReturn(0L);

        // when
        var result = useCase.execute(new CursorPageRequestDto(null, 10), new PostingListFilterDto(), actor());

        // then
        assertThat(result.page().cursor()).isNull();
        assertThat(result.page().totalCount()).isZero();
        assertThat(result.data()).isEmpty();
    }

    @Test
    @DisplayName("페이지 크기가 0이면 행과 커서를 내려주지 않는다")
    void zeroPageSizeReturnsNoRowsAndNoCursor() {
        // given
        given(postingQueryRepository.getCountOfPostings(any())).willReturn(3L);
        List<PostingListResponse> rows = List.of(row(1L));
        given(postingQueryRepository.getPostingsWithCursor(any(), any(), any())).willReturn(rows);

        // when
        var result = useCase.execute(new CursorPageRequestDto(null, 0), new PostingListFilterDto(), actor());

        // then
        assertThat(result.page().cursor()).isNull();
        assertThat(result.data()).isEmpty();
    }
}
