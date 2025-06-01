package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingKeywordListResponseDto;

import java.util.List;

public interface GetPostingKeywordListUseCase {
    List<PostingKeywordListResponseDto> execute();
}
