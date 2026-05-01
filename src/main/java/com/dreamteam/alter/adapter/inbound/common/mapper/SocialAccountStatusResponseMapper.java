package com.dreamteam.alter.adapter.inbound.common.mapper;

import com.dreamteam.alter.adapter.inbound.common.dto.SocialAccountStatusResponseDto;
import com.dreamteam.alter.domain.user.result.SocialAccountStatusResult;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SocialAccountStatusResponseMapper {

    public static SocialAccountStatusResponseDto toResponse(SocialAccountStatusResult result) {
        return SocialAccountStatusResponseDto.of(
            result.provider(),
            result.linked(),
            result.linkedAt()
        );
    }
}
