package com.dreamteam.alter.domain.workspace.port.inbound;

import java.util.List;

import com.dreamteam.alter.domain.workspace.entity.BusinessType;

public interface GetBusinessTypeListUseCase {
    List<BusinessType> execute();
}
