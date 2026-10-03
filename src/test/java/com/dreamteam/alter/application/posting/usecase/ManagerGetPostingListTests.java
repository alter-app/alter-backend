package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.posting.dto.ManagerPostingListFilterDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.ManagerPostingListResponse;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationQueryRepository;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagerGetPostingListTests {
    @Mock PostingQueryRepository postingQueryRepository;
    @Mock PostingApplicationQueryRepository postingApplicationQueryRepository;
    @Spy ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    @InjectMocks ManagerGetPostingList useCase;

    private ManagerPostingListResponse row(Long id) {
        ManagerPostingListResponse row = mock(ManagerPostingListResponse.class);
        Workspace workspace = mock(Workspace.class);
        given(workspace.getBusinessType()).willReturn(BusinessType.create("카페", "설명"));
        given(row.getWorkspace()).willReturn(workspace);
        given(row.getId()).willReturn(id);
        given(row.getStatus()).willReturn(PostingStatus.OPEN);
        given(row.getCreatedAt()).willReturn(LocalDateTime.of(2026, 10, 3, 12, 0));
        return row;
    }

    @Test
    void acceptedCountsAreFetchedOnceForWholePageAndMissingCountIsZero() {
        ManagerActor actor = mock(ManagerActor.class);
        given(actor.getManagerUser()).willReturn(mock(ManagerUser.class));
        given(postingQueryRepository.getManagerPostingCount(any(), any())).willReturn(2L);
        List<ManagerPostingListResponse> rows = List.of(row(1L), row(2L));
        given(postingQueryRepository.getManagerPostingsWithCursor(any(), any(), any()))
            .willReturn(rows);
        given(postingApplicationQueryRepository.countAcceptedByPostingIds(List.of(1L, 2L)))
            .willReturn(Map.of(1L, 3L));
        var result = useCase.execute(new CursorPageRequestDto(null, 10), new ManagerPostingListFilterDto(), actor);
        assertThat(result.data()).extracting(dto -> dto.getAcceptedCount()).containsExactly(3L, 0L);
        then(postingApplicationQueryRepository).should().countAcceptedByPostingIds(List.of(1L, 2L));
    }
}
