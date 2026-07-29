package com.dreamteam.alter.application.posting.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.command.UpdatePostingCommand;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.port.inbound.ManagerUpdatePostingUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingQueryRepository;
import com.dreamteam.alter.domain.posting.type.PostingStatus;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;

import lombok.RequiredArgsConstructor;

@Service("managerUpdatePosting")
@RequiredArgsConstructor
@Transactional
public class ManagerUpdatePosting implements ManagerUpdatePostingUseCase {

    private final PostingQueryRepository postingQueryRepository;

    @Override
    public void execute(Long postingId, UpdatePostingCommand command, ManagerActor actor) {
        ManagerUser managerUser = actor.getManagerUser();

        Posting posting = postingQueryRepository.findByManagerAndId(postingId, managerUser)
            .orElseThrow(() -> new CustomException(ErrorCode.POSTING_NOT_FOUND));

        // 삭제된 공고는 내용 수정 불가
        if (PostingStatus.DELETED.equals(posting.getStatus())) {
            throw new CustomException(ErrorCode.CONFLICT);
        }

        posting.updateContent(command);
    }
}
