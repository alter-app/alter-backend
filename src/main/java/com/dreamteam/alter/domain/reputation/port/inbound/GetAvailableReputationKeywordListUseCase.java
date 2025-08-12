package com.dreamteam.alter.domain.reputation.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.reputation.dto.AvailableReputationKeywordResponseDto;
import com.dreamteam.alter.domain.reputation.type.ReputationKeywordType;

public interface GetAvailableReputationKeywordListUseCase {
    AvailableReputationKeywordResponseDto execute(ReputationKeywordType keywordType);
}
