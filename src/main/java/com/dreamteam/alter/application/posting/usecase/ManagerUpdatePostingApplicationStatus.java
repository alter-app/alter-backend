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
import com.dreamteam.alter.domain.workspace.port.inbound.CreateWorkspaceWorkerUseCase;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("managerUpdatePostingApplicationStatus")
@RequiredArgsConstructor
@Transactional
public class ManagerUpdatePostingApplicationStatus implements ManagerUpdatePostingApplicationStatusUseCase {

    private final PostingApplicationQueryRepository postingApplicationQueryRepository;

    @Resource(name = "addWorkerToWorkspace")
    private final CreateWorkspaceWorkerUseCase createWorkspaceWorker;

    @Override
    public void execute(
        Long postingApplicationId,
        UpdatePostingApplicationStatusRequestDto request,
        ManagerActor actor
    ) {
        // SHORTLISTED, ACCEPTED, REJECTED 상태로만 변경 가능
        switch (request.getStatus()) {
            case SHORTLISTED, ACCEPTED, REJECTED -> {}
            default -> throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        ManagerUser managerUser = actor.getManagerUser();

        PostingApplication postingApplication =
            postingApplicationQueryRepository.getByManagerAndId(managerUser, postingApplicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.POSTING_APPLICATION_NOT_FOUND));

        // ACCEPTED, CANCELLED, REJECTED, EXPIRED 상태의 지원서는 상태 변경 불가
        switch (postingApplication.getStatus()) {
            case ACCEPTED, CANCELLED, REJECTED, EXPIRED ->
                throw new CustomException(ErrorCode.POSTING_APPLICATION_STATUS_NOT_UPDATABLE);
            default -> {}
        }

        postingApplication.updateStatus(request.getStatus());

        if (request.getStatus().equals(PostingApplicationStatus.ACCEPTED)) {
            createWorkspaceWorker.execute(
                postingApplication.getPosting().getWorkspace(),
                postingApplication.getUser()
            );
        }
    }

}
