package com.dreamteam.alter.domain.user.port.inbound;

import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.UserPostingApplicationListResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetUserPostingApplicationListUseCase {
    PaginatedResponseDto<UserPostingApplicationListResponseDto> execute(AppActor actor, PageRequestDto pageRequest);
}
