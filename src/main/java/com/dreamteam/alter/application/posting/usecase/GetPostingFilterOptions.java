package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingFilterOptionsResponseDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingFilterOptionsResponse;
import com.dreamteam.alter.domain.posting.port.inbound.GetPostingFilterOptionsUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.posting.type.PostingSortType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service("getPostingFilterOptions")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPostingFilterOptions implements GetPostingFilterOptionsUseCase {

    private final PostingQueryRepository postingQueryRepository;

    @Override
    public PostingFilterOptionsResponseDto execute() {
        PostingFilterOptionsResponse filterOptions = postingQueryRepository.getPostingFilterOptions();

        return PostingFilterOptionsResponseDto.of(
            filterOptions.getProvinces(),
            filterOptions.getDistricts(),
            filterOptions.getTowns(),
            Arrays.asList(PostingSortType.values())
        );
    }
}
