package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingFilterOptionsResponseDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingFilterOptionsResponse;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessTypeQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPostingFilterOptions 테스트")
class GetPostingFilterOptionsTests {

    @Mock
    private PostingQueryRepository postingQueryRepository;

    @Mock
    private BusinessTypeQueryRepository businessTypeQueryRepository;

    @InjectMocks
    private GetPostingFilterOptions getPostingFilterOptions;

    @Test
    @DisplayName("필터 옵션에 지역/정렬과 함께 업종 목록이 포함된다")
    void execute_업종포함() {
        // given
        given(postingQueryRepository.getPostingFilterOptions())
            .willReturn(PostingFilterOptionsResponse.of(List.of("서울특별시"), List.of("강남구"), List.of("역삼동")));
        given(businessTypeQueryRepository.findAll())
            .willReturn(List.of(BusinessType.create("카페", null), BusinessType.create("음식점", null)));

        // when
        PostingFilterOptionsResponseDto result = getPostingFilterOptions.execute();

        // then
        assertThat(result.getProvinces()).containsExactly("서울특별시");
        assertThat(result.getBusinessTypes()).extracting("name").containsExactly("카페", "음식점");
    }
}
