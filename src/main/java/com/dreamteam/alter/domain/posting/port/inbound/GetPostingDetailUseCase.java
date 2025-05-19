package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingDetailResponseDto;

public interface GetPostingDetailUseCase {
    PostingDetailResponseDto execute(Long postingId);
}
