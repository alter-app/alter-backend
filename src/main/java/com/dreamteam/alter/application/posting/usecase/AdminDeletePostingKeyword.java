package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.PostingKeyword;
import com.dreamteam.alter.domain.posting.port.inbound.AdminDeletePostingKeywordUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingKeywordQueryRepository;
import com.dreamteam.alter.domain.posting.port.outbound.PostingKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminDeletePostingKeyword")
@RequiredArgsConstructor
@Transactional
public class AdminDeletePostingKeyword implements AdminDeletePostingKeywordUseCase {

    private final PostingKeywordRepository postingKeywordRepository;
    private final PostingKeywordQueryRepository postingKeywordQueryRepository;

    @Override
    public void execute(Long id) {
        PostingKeyword keyword = postingKeywordRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.POSTING_KEYWORDS_NOT_FOUND));

        if (postingKeywordQueryRepository.existsPostingUsingKeyword(id)) {
            throw new CustomException(ErrorCode.CONFLICT, "공고에서 사용 중인 업종은 삭제할 수 없습니다.");
        }

        postingKeywordRepository.delete(keyword);
    }
}
