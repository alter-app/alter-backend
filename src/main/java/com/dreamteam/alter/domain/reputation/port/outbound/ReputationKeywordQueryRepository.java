package com.dreamteam.alter.domain.reputation.port.outbound;

import com.dreamteam.alter.domain.reputation.entity.ReputationKeyword;
import com.dreamteam.alter.domain.reputation.type.ReputationKeywordType;

import java.util.List;
import java.util.Set;

public interface ReputationKeywordQueryRepository {
    List<ReputationKeyword> findAllByKeywordType(ReputationKeywordType keywordType);
    List<ReputationKeyword> findByIdList(Set<String> keywordIds);
}
