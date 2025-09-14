package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingMapMarkerFilterDto;
import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingMapMarkerResponseDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingListForMapMarkerResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.port.inbound.GetPostingMapMarkersUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("getPostingMapMarkers")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPostingMapMarkers implements GetPostingMapMarkersUseCase {

    private final PostingQueryRepository postingQueryRepository;

    @Override
    public List<PostingMapMarkerResponseDto> execute(PostingMapMarkerFilterDto filter) {
        if (ObjectUtils.isEmpty(filter)) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "좌표 값은 필수입니다.");
        }

        if (ObjectUtils.isEmpty(filter.getCoordinate1()) || ObjectUtils.isEmpty(filter.getCoordinate2())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "좌표 값은 좌상단, 우하단 모두 포함해야합니다.");
        }

        List<PostingListForMapMarkerResponse> postingListForMapMarker =
            postingQueryRepository.getPostingListForMapMarker(filter);

        return postingListForMapMarker.stream()
            .map(PostingMapMarkerResponseDto::of)
            .toList();
    }
}
