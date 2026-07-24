package com.dreamteam.alter.application.posting.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.port.inbound.ManagerUpdatePostingUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;

import lombok.RequiredArgsConstructor;

@Service("managerUpdatePosting")
@RequiredArgsConstructor
@Transactional
public class ManagerUpdatePosting implements ManagerUpdatePostingUseCase {

    private final PostingQueryRepository postingQueryRepository;

    @Override
    public void execute(Long postingId, UpdatePostingRequestDto request, ManagerActor actor) {
        ManagerUser managerUser = actor.getManagerUser();

        Posting posting = postingQueryRepository.findByManagerAndId(postingId, managerUser)
            .orElseThrow(() -> new CustomException(ErrorCode.POSTING_NOT_FOUND));

        posting.updateContent(
            request.getTitle(),
            request.getDescription(),
            request.getPayAmount(),
            request.getPaymentType(),
            request.getCreateSchedules(),
            request.getUpdateSchedules(),
            request.getDeleteScheduleIds()
        );
    }
}
