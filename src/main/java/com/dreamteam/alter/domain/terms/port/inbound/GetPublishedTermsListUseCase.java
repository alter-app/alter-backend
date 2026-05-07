package com.dreamteam.alter.domain.terms.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.terms.dto.PublishedTermsItemResponseDto;

import java.util.List;

public interface GetPublishedTermsListUseCase {

    List<PublishedTermsItemResponseDto> execute();
}
