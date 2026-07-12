package com.dreamteam.alter.application.workspace.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.workspace.command.AdminUpdateBusinessTypeCommand;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.port.inbound.AdminUpdateBusinessTypeUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessTypeRepository;

import lombok.RequiredArgsConstructor;

@Service("adminUpdateBusinessType")
@RequiredArgsConstructor
@Transactional
public class AdminUpdateBusinessType implements AdminUpdateBusinessTypeUseCase {

    private final BusinessTypeRepository businessTypeRepository;

    @Override
    public void execute(Long id, AdminUpdateBusinessTypeCommand command) {
        BusinessType businessType = businessTypeRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 업종입니다."));

        if (businessTypeRepository.existsByNameAndIdNot(command.name(), id)) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 존재하는 업종입니다.");
        }

        businessType.update(command.name(), command.description());
    }
}
