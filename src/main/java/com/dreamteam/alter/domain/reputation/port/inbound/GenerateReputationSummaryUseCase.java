package com.dreamteam.alter.domain.reputation.port.inbound;

import com.dreamteam.alter.domain.reputation.type.ReputationType;

import java.util.List;

public interface GenerateReputationSummaryUseCase {
    void execute(ReputationType targetType, List<Long> targetIds);
}
