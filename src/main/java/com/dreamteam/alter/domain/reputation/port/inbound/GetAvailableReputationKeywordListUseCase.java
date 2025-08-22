package com.dreamteam.alter.domain.reputation.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.AvailableReputationKeywordRequestDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.AvailableReputationKeywordResponseDto;

public interface GetAvailableReputationKeywordListUseCase {
    AvailableReputationKeywordResponseDto execute(AvailableReputationKeywordRequestDto keywordRequestDto);
}
