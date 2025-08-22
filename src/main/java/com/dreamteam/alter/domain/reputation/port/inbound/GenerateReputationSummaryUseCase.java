package com.dreamteam.alter.domain.reputation.port.inbound;

import com.dreamteam.alter.domain.reputation.type.ReputationType;

public interface GenerateReputationSummaryUseCase {
    void execute(ReputationType targetType, Long targetId);
}
