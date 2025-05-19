package com.dreamteam.alter.domain.posting.port.outbound;

import com.dreamteam.alter.domain.posting.entity.Keyword;

import java.util.List;

public interface KeywordQueryRepository {
    List<Keyword> findByIds(List<Long> keywordIds);
}
