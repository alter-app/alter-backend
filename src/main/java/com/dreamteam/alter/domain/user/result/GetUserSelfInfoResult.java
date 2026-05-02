package com.dreamteam.alter.domain.user.result;

import com.dreamteam.alter.adapter.outbound.user.persistence.readonly.UserSelfInfoResponse;
import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;

import java.time.LocalDateTime;

public record GetUserSelfInfoResult(
    Long id,
    String name,
    String nickname,
    LocalDateTime createdAt,
    ReputationSummary reputationSummary
) {

    public static GetUserSelfInfoResult from(UserSelfInfoResponse response) {
        return new GetUserSelfInfoResult(
            response.getId(),
            response.getName(),
            response.getNickname(),
            response.getCreatedAt(),
            response.getReputationSummary()
        );
    }
}
