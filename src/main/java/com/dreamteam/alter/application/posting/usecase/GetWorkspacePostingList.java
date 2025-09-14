package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingListResponseDto;
import com.dreamteam.alter.domain.posting.port.inbound.GetWorkspacePostingListUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.user.context.AppActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("getWorkspacePostingList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetWorkspacePostingList implements GetWorkspacePostingListUseCase {

    private final PostingQueryRepository postingQueryRepository;

    @Override
    public List<PostingListResponseDto> execute(Long workspaceId, AppActor actor) {
        return postingQueryRepository.getWorkspacePostingList(workspaceId, actor.getUser())
            .stream()
            .map(PostingListResponseDto::from)
            .toList();
    }
}
