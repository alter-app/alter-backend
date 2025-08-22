package com.dreamteam.alter.domain.reputation.port.outbound;

import com.dreamteam.alter.domain.reputation.entity.Reputation;

public interface ReputationRepository {
    Reputation save(Reputation reputation);
}
