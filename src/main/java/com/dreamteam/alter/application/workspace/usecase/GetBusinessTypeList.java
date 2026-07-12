package com.dreamteam.alter.application.workspace.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.port.inbound.GetBusinessTypeListUseCase;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessTypeQueryRepository;

import lombok.RequiredArgsConstructor;

@Service("getBusinessTypeList")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetBusinessTypeList implements GetBusinessTypeListUseCase {

    private final BusinessTypeQueryRepository businessTypeQueryRepository;

    @Override
    public List<BusinessType> execute() {
        return businessTypeQueryRepository.findAll();
    }
}
