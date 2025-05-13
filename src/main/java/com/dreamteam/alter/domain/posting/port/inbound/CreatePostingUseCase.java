package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.CreatePostingRequestDto;

public interface CreatePostingUseCase {
    void execute(CreatePostingRequestDto request);
}
