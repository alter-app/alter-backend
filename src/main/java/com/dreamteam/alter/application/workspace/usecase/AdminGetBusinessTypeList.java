package com.dreamteam.alter.application.workspace.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.port.inbound.AdminGetBusinessTypeListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessTypeQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("adminGetBusinessTypeList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGetBusinessTypeList implements AdminGetBusinessTypeListUseCase {

    private final BusinessTypeQueryRepository businessTypeQueryRepository;

    @Override
    public List<BusinessType> execute() {
        return businessTypeQueryRepository.findAll();
    }
}
