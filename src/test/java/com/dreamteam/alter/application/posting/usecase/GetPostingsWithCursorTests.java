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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
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
    void lastPageShorterThanPageSizeHasNoCursor() {
        given(postingQueryRepository.getCountOfPostings(any())).willReturn(2L);
        List<PostingListResponse> rows = List.of(row(1L), row(2L));
        given(postingQueryRepository.getPostingsWithCursor(any(), any(), any()))
            .willReturn(rows);

        var result = useCase.execute(new CursorPageRequestDto(null, 10), new PostingListFilterDto(), actor());

        assertThat(result.page().cursor()).isNull();
        assertThat(result.page().pageSize()).isEqualTo(10);
        assertThat(result.page().totalCount()).isEqualTo(2);
        assertThat(result.data()).hasSize(2);
    }

    @Test
    void lastPageExactlyPageSizeHasNoCursor() {
        given(postingQueryRepository.getCountOfPostings(any())).willReturn(2L);
        List<PostingListResponse> rows = List.of(row(1L), row(2L));
        given(postingQueryRepository.getPostingsWithCursor(any(), any(), any()))
            .willReturn(rows);

        var result = useCase.execute(new CursorPageRequestDto(null, 2), new PostingListFilterDto(), actor());

        assertThat(result.page().cursor()).isNull();
        assertThat(result.data()).hasSize(2);
    }

    @Test
    void middlePageDropsExtraRowAndPointsCursorAtLastReturnedRow() {
        given(postingQueryRepository.getCountOfPostings(any())).willReturn(3L);
        List<PostingListResponse> rows = List.of(row(1L), row(2L), row(3L));
        given(postingQueryRepository.getPostingsWithCursor(any(), any(), any()))
            .willReturn(rows);

        var result = useCase.execute(new CursorPageRequestDto(null, 2), new PostingListFilterDto(), actor());

        assertThat(result.data()).extracting(dto -> dto.getId()).containsExactly(1L, 2L);
        assertThat(result.page().totalCount()).isEqualTo(3);
        CursorDto cursor = CursorUtil.decodeCursor(result.page().cursor(), CursorDto.class, objectMapper);
        assertThat(cursor.getId()).isEqualTo(2L);
    }

    @Test
    void repositoryIsAskedForOneMoreRowThanPageSize() {
        given(postingQueryRepository.getCountOfPostings(any())).willReturn(1L);
        List<PostingListResponse> rows = List.of(row(1L));
        given(postingQueryRepository.getPostingsWithCursor(any(), any(), any()))
            .willReturn(rows);

        useCase.execute(new CursorPageRequestDto(null, 5), new PostingListFilterDto(), actor());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<CursorPageRequest<CursorDto>> captor = ArgumentCaptor.forClass(CursorPageRequest.class);
        verify(postingQueryRepository).getPostingsWithCursor(captor.capture(), any(), any());
        assertThat(captor.getValue().pageSize()).isEqualTo(6);
    }

    @Test
    void noMatchingPostingsReturnsEmptyWithoutCursor() {
        given(postingQueryRepository.getCountOfPostings(any())).willReturn(0L);

        var result = useCase.execute(new CursorPageRequestDto(null, 10), new PostingListFilterDto(), actor());

        assertThat(result.page().cursor()).isNull();
        assertThat(result.page().totalCount()).isZero();
        assertThat(result.data()).isEmpty();
    }

    @Test
    void zeroPageSizeReturnsNoRowsAndNoCursor() {
        given(postingQueryRepository.getCountOfPostings(any())).willReturn(3L);
        List<PostingListResponse> rows = List.of(row(1L));
        given(postingQueryRepository.getPostingsWithCursor(any(), any(), any())).willReturn(rows);

        var result = useCase.execute(new CursorPageRequestDto(null, 0), new PostingListFilterDto(), actor());

        assertThat(result.page().cursor()).isNull();
        assertThat(result.data()).isEmpty();
    }
}
