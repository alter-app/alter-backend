package com.dreamteam.alter.domain.terms.port.outbound;

import com.dreamteam.alter.domain.terms.entity.Terms;
import com.dreamteam.alter.domain.terms.type.TermsStatus;
import com.dreamteam.alter.domain.terms.type.TermsType;

import java.util.List;
import java.util.Optional;

public interface TermsQueryRepository {

    Optional<Terms> findById(Long id);

    long countByFilter(TermsType type, TermsStatus status);

    List<Terms> findByFilter(TermsType type, TermsStatus status, int page, int pageSize);

    Optional<Terms> findPublishedByType(TermsType type);

    Optional<Terms> findPublishedByTypeWithLock(TermsType type);

    /**
     * 각 TermsType 별로 PUBLISHED 상태인 약관 중 effective_at 이 가장 최근인 것 1건씩 반환
     * 결과 순서: TermsType 선언 순서 (SERVICE, PRIVACY, LOCATION, MARKETING)
     */
    List<Terms> findLatestPublishedPerType();
}
