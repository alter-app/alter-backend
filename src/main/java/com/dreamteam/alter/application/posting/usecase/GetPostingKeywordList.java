package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.PostingKeywordListResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.PostingKeyword;
import com.dreamteam.alter.domain.posting.port.inbound.GetPostingKeywordListUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingKeywordQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("getPostingKeywordList")
@RequiredArgsConstructor
public class GetPostingKeywordList implements GetPostingKeywordListUseCase {

    private final PostingKeywordQueryRepository postingKeywordQueryRepository;

    @Override
    public List<PostingKeywordListResponseDto> execute() {
        List<PostingKeyword> postingKeywords = postingKeywordQueryRepository.findAll();

        if (ObjectUtils.isEmpty(postingKeywords)) {
            throw new CustomException(ErrorCode.POSTING_KEYWORDS_NOT_FOUND);
        }

        return postingKeywords.stream()
            .map(PostingKeywordListResponseDto::from)
            .toList();
    }

}
