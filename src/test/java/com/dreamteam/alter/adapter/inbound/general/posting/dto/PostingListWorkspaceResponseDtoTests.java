package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import com.dreamteam.alter.domain.workspace.entity.BusinessTypeFixture;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("PostingListWorkspaceResponseDto 테스트")
class PostingListWorkspaceResponseDtoTests {

    @Test
    @DisplayName("업장 정보를 목록 응답 DTO로 변환한다")
    void createsDtoFromWorkspace() {
        // given
        Workspace workspace = mock(Workspace.class);
        given(workspace.getId()).willReturn(1L);
        given(workspace.getBusinessName()).willReturn("카페 알터");
        given(workspace.getBusinessType()).willReturn(BusinessTypeFixture.of(false));
        given(workspace.getProvince()).willReturn("서울특별시");
        given(workspace.getDistrict()).willReturn("강남구");
        given(workspace.getTown()).willReturn("역삼동");
        given(workspace.getLatitude()).willReturn(new BigDecimal("37.566500"));
        given(workspace.getLongitude()).willReturn(new BigDecimal("126.978000"));

        // when
        PostingListWorkspaceResponseDto response = PostingListWorkspaceResponseDto.from(workspace);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getBusinessName()).isEqualTo("카페 알터");
        assertThat(response.getBusinessType()).isEqualTo("카페");
        assertThat(response.getProvince()).isEqualTo("서울특별시");
        assertThat(response.getDistrict()).isEqualTo("강남구");
        assertThat(response.getTown()).isEqualTo("역삼동");
        assertThat(response.getLatitude()).isEqualByComparingTo("37.566500");
        assertThat(response.getLongitude()).isEqualByComparingTo("126.978000");
    }
}
