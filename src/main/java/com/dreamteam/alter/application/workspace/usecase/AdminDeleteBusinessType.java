package com.dreamteam.alter.application.workspace.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.port.inbound.AdminDeleteBusinessTypeUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessTypeQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessTypeRepository;

import lombok.RequiredArgsConstructor;

@Service("adminDeleteBusinessType")
@RequiredArgsConstructor
@Transactional
public class AdminDeleteBusinessType implements AdminDeleteBusinessTypeUseCase {

    private final BusinessTypeRepository businessTypeRepository;
    private final BusinessTypeQueryRepository businessTypeQueryRepository;

    @Override
    public void execute(Long id) {
        BusinessType businessType = businessTypeRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 업종입니다."));

        if (businessType.isRequiresDetail()) {
            throw new CustomException(ErrorCode.CONFLICT, "'기타' 업종은 삭제할 수 없습니다.");
        }

        if (businessTypeQueryRepository.existsWorkspaceUsingBusinessType(id)
            || businessTypeQueryRepository.existsWorkspaceRequestUsingBusinessType(id)) {
            throw new CustomException(ErrorCode.CONFLICT, "사용 중인 업종은 삭제할 수 없습니다.");
        }

        businessTypeRepository.delete(businessType);
    }
}
