package com.dreamteam.alter.domain.posting.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingMapMarkerFilterDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingMapMarkerResponseDto;

import java.util.List;

public interface GetPostingMapMarkersUseCase {
    List<PostingMapMarkerResponseDto> execute(PostingMapMarkerFilterDto filter);
}
