package com.dreamteam.alter.application.posting.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.posting.entity.PostingKeyword;
import com.dreamteam.alter.domain.posting.port.outbound.PostingKeywordQueryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PostingKeywordResolver {

    private final PostingKeywordQueryRepository postingKeywordQueryRepository;

    /**
     * 마스터 업종 ID 목록을 조회·검증한다.
     * 비어 있으면 빈 리스트를 반환하고, 조회 결과 수가 요청 수와 다르면 존재하지 않는 ID가 있는 것으로 보아 예외.
     * @param keywordIds 마스터 업종 ID 목록 (nullable)
     * @return 검증된 PostingKeyword 목록
     */
    public List<PostingKeyword> resolveAndValidate(List<Long> keywordIds) {
        if (ObjectUtils.isEmpty(keywordIds)) {
            return List.of();
        }

        List<PostingKeyword> keywords = postingKeywordQueryRepository.findByIds(keywordIds);
        if (keywords.size() != keywordIds.size()) {
            throw new CustomException(ErrorCode.INVALID_KEYWORD);
        }
        return keywords;
    }
}
