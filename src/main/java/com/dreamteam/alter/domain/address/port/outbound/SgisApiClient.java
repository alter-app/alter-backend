package com.dreamteam.alter.domain.address.port.outbound;

import com.dreamteam.alter.adapter.outbound.address.external.dto.SgisAuthResponse;
import com.dreamteam.alter.domain.address.model.SgisStageAddress;

import java.util.List;

public interface SgisApiClient {

    SgisAuthResponse authenticate();

    List<SgisStageAddress> getStageAddresses(String accessToken, String code);
}
