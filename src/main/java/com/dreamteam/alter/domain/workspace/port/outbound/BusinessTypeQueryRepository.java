package com.dreamteam.alter.domain.workspace.port.outbound;

import java.util.List;

import com.dreamteam.alter.domain.workspace.entity.BusinessType;

public interface BusinessTypeQueryRepository {
    List<BusinessType> findAll();

    // 업장 또는 업장 신청 어느 쪽에서든 해당 업종을 참조 중인지 여부
    boolean existsReferenced(Long businessTypeId);
}
