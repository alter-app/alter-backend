package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.AvailableReputationKeywordRequestDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.AvailableReputationKeywordResponseDto;
import com.dreamteam.alter.domain.reputation.port.inbound.GetAvailableReputationKeywordListUseCase;
import com.dreamteam.alter.domain.reputation.port.outbound.ReputationKeywordQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("getAvailableReputationKeywordList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAvailableReputationKeywordList implements GetAvailableReputationKeywordListUseCase {

    private final ReputationKeywordQueryRepository reputationKeywordQueryRepository;

    @Override
    public AvailableReputationKeywordResponseDto execute(AvailableReputationKeywordRequestDto keywordRequestDto) {
        return AvailableReputationKeywordResponseDto.of(
            reputationKeywordQueryRepository.findAllByKeywordType(keywordRequestDto.getKeywordType())
        );
    }

}
