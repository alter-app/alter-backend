package com.dreamteam.alter.domain.reputation.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.reputation.ReputationSummaryData;
import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;

public interface UpdateReputationSummaryDescriptionAsyncUseCase {
    void execute(ReputationSummary reputationSummary, ReputationSummaryData summaryData);
}
