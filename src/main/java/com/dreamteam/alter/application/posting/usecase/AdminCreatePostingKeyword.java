package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.command.AdminCreatePostingKeywordCommand;
import com.dreamteam.alter.domain.posting.entity.PostingKeyword;
import com.dreamteam.alter.domain.posting.port.inbound.AdminCreatePostingKeywordUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminCreatePostingKeyword")
@RequiredArgsConstructor
@Transactional
public class AdminCreatePostingKeyword implements AdminCreatePostingKeywordUseCase {

    private final PostingKeywordRepository postingKeywordRepository;

    @Override
    public PostingKeyword execute(AdminCreatePostingKeywordCommand command) {
        if (postingKeywordRepository.existsByName(command.name())) {
            throw new CustomException(ErrorCode.INVALID_KEYWORD, "이미 존재하는 업종입니다.");
        }

        PostingKeyword keyword = PostingKeyword.create(command.name(), command.description());
        return postingKeywordRepository.save(keyword);
    }
}
