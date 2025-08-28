package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.manager.posting.dto.ManagerPostingDetailResponseDto;
import com.dreamteam.alter.adapter.outbound.posting.persistence.readonly.ManagerPostingDetailResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.port.inbound.ManagerGetPostingDetailUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("managerGetPostingDetail")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerGetPostingDetail implements ManagerGetPostingDetailUseCase {

    private final PostingQueryRepository postingQueryRepository;

    @Override
    public ManagerPostingDetailResponseDto execute(Long postingId, ManagerActor actor) {
        ManagerUser managerUser = actor.getManagerUser();

        ManagerPostingDetailResponse postingDetail = postingQueryRepository
            .getManagerPostingDetail(postingId, managerUser)
            .orElseThrow(() -> new CustomException(ErrorCode.POSTING_NOT_FOUND));

        return ManagerPostingDetailResponseDto.from(postingDetail);
    }
}
