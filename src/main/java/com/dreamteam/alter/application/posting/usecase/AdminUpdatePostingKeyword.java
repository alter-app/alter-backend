package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.command.AdminUpdatePostingKeywordCommand;
import com.dreamteam.alter.domain.posting.entity.PostingKeyword;
import com.dreamteam.alter.domain.posting.port.inbound.AdminUpdatePostingKeywordUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminUpdatePostingKeyword")
@RequiredArgsConstructor
@Transactional
public class AdminUpdatePostingKeyword implements AdminUpdatePostingKeywordUseCase {

    private final PostingKeywordRepository postingKeywordRepository;

    @Override
    public void execute(Long id, AdminUpdatePostingKeywordCommand command) {
        PostingKeyword keyword = postingKeywordRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.POSTING_KEYWORDS_NOT_FOUND));

        if (postingKeywordRepository.existsByNameAndIdNot(command.name(), id)) {
            throw new CustomException(ErrorCode.INVALID_KEYWORD, "이미 존재하는 업종입니다.");
        }

        keyword.update(command.name(), command.description());
    }
}
