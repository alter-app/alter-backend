package com.dreamteam.alter.application.posting;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.CreatePostingRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.PostingKeyword;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.port.inbound.CreatePostingUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingKeywordQueryRepository;
import com.dreamteam.alter.domain.posting.port.outbound.PostingRepository;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("createPosting")
@RequiredArgsConstructor
@Transactional
public class CreatePosting implements CreatePostingUseCase {

    private final PostingRepository postingRepository;
    private final PostingKeywordQueryRepository postingKeywordQueryRepository;
    private final WorkspaceQueryRepository workspaceQueryRepository;

    @Override
    public void execute(CreatePostingRequestDto request) {
        if (ObjectUtils.isEmpty(request.getKeywords())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        List<PostingKeyword> postingKeywords = postingKeywordQueryRepository.findByIds(request.getKeywords());
        if (BooleanUtils.isFalse(postingKeywords.size() == request.getKeywords().size())) {
            throw new CustomException(ErrorCode.INVALID_KEYWORD);
        }

        Workspace workspace = workspaceQueryRepository.findById(request.getWorkspaceId())
            .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

        Posting posting = Posting.create(request, workspace, postingKeywords);
        postingRepository.save(posting);
    }

}
