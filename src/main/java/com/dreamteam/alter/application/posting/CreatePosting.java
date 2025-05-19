package com.dreamteam.alter.application.posting;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.CreatePostingRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.Keyword;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.port.inbound.CreatePostingUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.KeywordQueryRepository;
import com.dreamteam.alter.domain.posting.port.outbound.PostingRepository;
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
    private final KeywordQueryRepository keywordQueryRepository;

    @Override
    public void execute(CreatePostingRequestDto request) {
        if (ObjectUtils.isEmpty(request.getKeywords())) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        List<Keyword> keywords = keywordQueryRepository.findByIds(request.getKeywords());
        if (BooleanUtils.isFalse(keywords.size() == request.getKeywords().size())) {
            throw new CustomException(ErrorCode.INVALID_KEYWORD);
        }

        Posting posting = Posting.create(request, keywords);
        postingRepository.save(posting);
    }

}
