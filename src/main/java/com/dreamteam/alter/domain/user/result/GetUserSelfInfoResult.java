package com.dreamteam.alter.domain.user.result;

import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;

import java.time.LocalDateTime;

public record GetUserSelfInfoResult(
    Long id,
    String name,
    String nickname,
    LocalDateTime createdAt,
    ReputationSummary reputationSummary
) {
}
