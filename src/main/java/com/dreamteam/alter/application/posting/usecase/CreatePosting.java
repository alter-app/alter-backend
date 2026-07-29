package com.dreamteam.alter.application.posting.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.command.CreatePostingCommand;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.port.inbound.CreatePostingUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("createPosting")
@RequiredArgsConstructor
@Transactional
public class CreatePosting implements CreatePostingUseCase {

    private final PostingRepository postingRepository;
    private final WorkspaceQueryRepository workspaceQueryRepository;

    @Override
    public void execute(CreatePostingCommand command, ManagerActor actor) {
        Workspace workspace =
            workspaceQueryRepository.findByIdAndManagerUser(command.workspaceId(), actor.getManagerUser())
                .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

        Posting posting = Posting.create(command, workspace);
        postingRepository.save(posting);
    }

}
