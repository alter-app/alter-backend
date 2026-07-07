package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.domain.posting.entity.PostingKeyword;
import com.dreamteam.alter.domain.posting.port.inbound.AdminGetPostingKeywordListUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingKeywordQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("adminGetPostingKeywordList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGetPostingKeywordList implements AdminGetPostingKeywordListUseCase {

    private final PostingKeywordQueryRepository postingKeywordQueryRepository;

    @Override
    public List<PostingKeyword> execute() {
        return postingKeywordQueryRepository.findAll();
    }
}
