package com.dreamteam.alter.application.workspace.usecase;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.workspace.command.AdminCreateBusinessTypeCommand;
import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.port.inbound.AdminCreateBusinessTypeUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessTypeRepository;

import lombok.RequiredArgsConstructor;

@Service("adminCreateBusinessType")
@RequiredArgsConstructor
@Transactional
public class AdminCreateBusinessType implements AdminCreateBusinessTypeUseCase {

    private final BusinessTypeRepository businessTypeRepository;

    @Override
    public BusinessType execute(AdminCreateBusinessTypeCommand command) {
        if (businessTypeRepository.existsByName(command.name())) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 존재하는 업종입니다.");
        }

        BusinessType businessType = BusinessType.create(command.name(), command.description());
        try {
            return businessTypeRepository.save(businessType);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 존재하는 업종입니다.");
        }
    }
}
