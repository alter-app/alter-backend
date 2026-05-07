package com.dreamteam.alter.application.terms.usecase;

import com.dreamteam.alter.adapter.inbound.general.terms.dto.PublishedTermsItemResponseDto;
import com.dreamteam.alter.domain.terms.port.inbound.GetPublishedTermsListUseCase;
import com.dreamteam.alter.domain.terms.port.outbound.TermsQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("getPublishedTermsList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPublishedTermsList implements GetPublishedTermsListUseCase {

    private final TermsQueryRepository termsQueryRepository;

    @Override
    public List<PublishedTermsItemResponseDto> execute() {
        return termsQueryRepository.findLatestPublishedPerType()
                .stream()
                .map(PublishedTermsItemResponseDto::from)
                .toList();
    }
}
