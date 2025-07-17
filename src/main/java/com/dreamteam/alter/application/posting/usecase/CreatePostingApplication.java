package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.CreatePostingApplicationRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.PostingApplication;
import com.dreamteam.alter.domain.posting.entity.PostingSchedule;
import com.dreamteam.alter.domain.posting.port.inbound.CreatePostingApplicationUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingApplicationRepository;
import com.dreamteam.alter.domain.posting.port.outbound.PostingScheduleQueryRepository;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerReadOnlyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("createPostingApplication")
@RequiredArgsConstructor
@Transactional
public class CreatePostingApplication implements CreatePostingApplicationUseCase {

    private final PostingScheduleQueryRepository postingScheduleQueryRepository;
    private final PostingApplicationRepository postingApplicationRepository;
    private final WorkspaceWorkerReadOnlyRepository workspaceWorkerReadOnlyRepository;

    @Override
    public void execute(AppActor actor, Long postingId, CreatePostingApplicationRequestDto request) {
        PostingSchedule postingSchedule =
            postingScheduleQueryRepository.findByIdAndPostingId(postingId, request.getPostingScheduleId())
                .orElseThrow(() -> new CustomException(ErrorCode.POSTING_SCHEDULE_NOT_FOUND));

        if (workspaceWorkerReadOnlyRepository.findActiveWorkerByWorkspaceAndUser(
                postingSchedule.getPosting().getWorkspace(),
                actor.getUser()
            )
            .isPresent()
        ) {
            throw new CustomException(ErrorCode.WORKSPACE_WORKER_ALREADY_EXISTS);
        }

        postingApplicationRepository.save(
            PostingApplication.create(postingSchedule, actor.getUser(), request.getDescription())
        );
    }

}
