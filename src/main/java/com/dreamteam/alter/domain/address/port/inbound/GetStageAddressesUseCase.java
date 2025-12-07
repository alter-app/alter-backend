package com.dreamteam.alter.domain.address.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.address.dto.StageAddressRequestDto;
import com.dreamteam.alter.domain.address.model.SgisStageAddress;

import java.util.List;

public interface GetStageAddressesUseCase {
    List<SgisStageAddress> execute(StageAddressRequestDto request);
}
