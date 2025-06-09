package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingDetailResponseDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.PostingDetailResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.port.inbound.GetPostingDetailUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.user.context.AppActor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("getPostingDetail")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPostingDetail implements GetPostingDetailUseCase {

    private final PostingQueryRepository postingQueryRepository;

    @Override
    public PostingDetailResponseDto execute(Long postingId, AppActor actor) {
        PostingDetailResponse postingDetail = postingQueryRepository.getPostingDetail(postingId, actor.getUser());
        if (ObjectUtils.isEmpty(postingDetail)) {
            throw new CustomException(ErrorCode.POSTING_NOT_FOUND);
        }

        return PostingDetailResponseDto.from(postingDetail);
    }

}
