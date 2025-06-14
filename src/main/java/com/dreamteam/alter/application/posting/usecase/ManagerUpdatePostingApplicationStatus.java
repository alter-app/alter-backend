package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.manager.posting.dto.UpdatePostingApplicationStatusRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.PostingApplication;
import com.dreamteam.alter.domain.posting.port.inbound.ManagerUpdatePostingApplicationStatusUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationQueryRepository;
import com.dreamteam.alter.domain.posting.type.PostingApplicationStatus;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("managerUpdatePostingApplicationStatus")
@RequiredArgsConstructor
@Transactional
public class ManagerUpdatePostingApplicationStatus implements ManagerUpdatePostingApplicationStatusUseCase {

    private final PostingApplicationQueryRepository postingApplicationQueryRepository;

    @Override
    public void execute(
        Long postingApplicationId,
        UpdatePostingApplicationStatusRequestDto request,
        ManagerActor actor
    ) {
        // SHORTLISTED, ACCEPTED 상태로만 변경 가능
        if (!request.getStatus().equals(PostingApplicationStatus.SHORTLISTED) &&
            !request.getStatus().equals(PostingApplicationStatus.ACCEPTED)) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        ManagerUser managerUser = actor.getManagerUser();

        PostingApplication postingApplication =
            postingApplicationQueryRepository.getByManagerAndId(managerUser, postingApplicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.POSTING_APPLICATION_NOT_FOUND));

        // 이미 ACCEPTED 상태인 경우, 취소됐거나 만료된 경우 상태 변경 불가
        PostingApplicationStatus status = postingApplication.getStatus();
        if (status.equals(PostingApplicationStatus.ACCEPTED) ||
            status.equals(PostingApplicationStatus.CANCELLED) ||
            status.equals(PostingApplicationStatus.EXPIRED)) {
            throw new CustomException(ErrorCode.POSTING_APPLICATION_STATUS_NOT_UPDATABLE);
        }

        postingApplication.updateStatus(request.getStatus());
    }

}
