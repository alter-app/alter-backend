package com.dreamteam.alter.adapter.inbound.general.reputation.dto;

import com.dreamteam.alter.domain.reputation.type.ReputationType;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReputationRequestListRequestDto {

    private ReputationType targetType;

    private Long targetId;

    public static ReputationRequestListRequestDto of(ReputationType targetType, Long targetId) {
        return new ReputationRequestListRequestDto(targetType, targetId);
    }

}
