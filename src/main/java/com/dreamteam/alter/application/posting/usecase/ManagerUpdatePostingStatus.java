package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingStatusRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.port.inbound.ManagerUpdatePostingStatusUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("managerUpdatePostingStatus")
@RequiredArgsConstructor
@Transactional
public class ManagerUpdatePostingStatus implements ManagerUpdatePostingStatusUseCase {

    private final PostingQueryRepository postingQueryRepository;

    @Override
    public void execute(Long postingId, UpdatePostingStatusRequestDto request, ManagerActor actor) {
        ManagerUser managerUser = actor.getManagerUser();

        Posting posting = postingQueryRepository.findByManagerAndId(postingId, managerUser)
            .orElseThrow(() -> new CustomException(ErrorCode.POSTING_NOT_FOUND));

        // DELETED 상태인 경우 상태 변경 불가
        if (PostingStatus.DELETED.equals(posting.getStatus())) {
            throw new CustomException(ErrorCode.CONFLICT);
        }

        posting.updateStatus(request.getStatus());
    }
}
