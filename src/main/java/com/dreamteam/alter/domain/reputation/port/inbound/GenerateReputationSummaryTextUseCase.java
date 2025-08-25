package com.dreamteam.alter.domain.reputation.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.reputation.ReputationSummaryData;

public interface GenerateReputationSummaryTextUseCase {
    String execute(ReputationSummaryData summaryData);
}
