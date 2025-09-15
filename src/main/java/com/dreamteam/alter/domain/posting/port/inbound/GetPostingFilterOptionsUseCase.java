package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingFilterOptionsResponseDto;

public interface GetPostingFilterOptionsUseCase {
    PostingFilterOptionsResponseDto execute();
}
