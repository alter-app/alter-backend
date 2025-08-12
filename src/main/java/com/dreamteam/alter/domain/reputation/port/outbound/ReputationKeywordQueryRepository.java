package com.dreamteam.alter.domain.reputation.port.outbound;

import com.dreamteam.alter.domain.reputation.entity.ReputationKeyword;
import com.dreamteam.alter.domain.reputation.type.ReputationKeywordType;

import java.util.List;

public interface ReputationKeywordQueryRepository {
    List<ReputationKeyword> findAllByKeywordType(ReputationKeywordType keywordType);
}
