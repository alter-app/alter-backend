package com.dreamteam.alter.domain.posting.port.outbound;

import com.dreamteam.alter.domain.posting.entity.PostingKeyword;

import java.util.List;

public interface PostingKeywordQueryRepository {
    List<PostingKeyword> findByIds(List<Long> keywordIds);
    List<PostingKeyword> findAll();
}
