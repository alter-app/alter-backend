package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.workspace.entity.BusinessJoinRequest;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessJoinRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BusinessJoinRequestRepositoryImpl implements BusinessJoinRequestRepository {

    private final BusinessJoinRequestJpaRepository businessJoinRequestJpaRepository;

    @Override
    public void save(BusinessJoinRequest request) {
        try {
            businessJoinRequestJpaRepository.saveAndFlush(request);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 합류 요청이 진행 중인 업장입니다.");
        }
    }
}
