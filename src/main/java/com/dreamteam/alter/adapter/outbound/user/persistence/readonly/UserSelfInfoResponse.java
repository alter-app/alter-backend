package com.dreamteam.alter.adapter.outbound.user.persistence.readonly;

import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSelfInfoResponse {

    private Long id;

    private String name;

    private String nickname;

    private LocalDateTime createdAt;

    private ReputationSummary reputationSummary;

}
