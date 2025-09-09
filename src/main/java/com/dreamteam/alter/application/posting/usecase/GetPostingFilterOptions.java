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
import java.util.List;
import java.util.stream.Collectors;

@Service("getPostingFilterOptions")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPostingFilterOptions implements GetPostingFilterOptionsUseCase {

    private final PostingQueryRepository postingQueryRepository;

    @Override
    public PostingFilterOptionsResponseDto execute() {
        PostingFilterOptionsResponse filterOptions = postingQueryRepository.getPostingFilterOptions();
        List<String> sortOptions = Arrays.stream(PostingSortType.values())
            .map(Enum::name)
            .collect(Collectors.toList());

        return PostingFilterOptionsResponseDto.builder()
            .provinces(filterOptions.getProvinces())
            .districts(filterOptions.getDistricts())
            .towns(filterOptions.getTowns())
            .sortOptions(sortOptions)
            .build();
    }
}
