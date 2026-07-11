package com.dreamteam.alter.application.posting.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.CreatePostingRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.entity.PostingKeyword;
import com.dreamteam.alter.domain.posting.port.inbound.CreatePostingUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingRepository;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("createPosting")
@RequiredArgsConstructor
@Transactional
public class CreatePosting implements CreatePostingUseCase {

    private final PostingRepository postingRepository;
    private final PostingKeywordResolver postingKeywordResolver;
    private final WorkspaceQueryRepository workspaceQueryRepository;

    @Override
    public void execute(CreatePostingRequestDto request) {
        List<PostingKeyword> postingKeywords = postingKeywordResolver.resolveAndValidate(request.getKeywords());

        Workspace workspace = workspaceQueryRepository.findById(request.getWorkspaceId())
            .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

        Posting posting = Posting.create(request, workspace, postingKeywords);
        postingRepository.save(posting);
    }

}
