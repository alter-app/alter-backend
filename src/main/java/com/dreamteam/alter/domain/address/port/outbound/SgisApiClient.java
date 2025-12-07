package com.dreamteam.alter.domain.address.port.outbound;

import com.dreamteam.alter.adapter.outbound.address.external.dto.SgisAuthResponse;
import com.dreamteam.alter.adapter.outbound.address.external.dto.SgisStageAddressResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface SgisApiClient {

    SgisAuthResponse authenticate() throws JsonProcessingException;

    SgisStageAddressResponse getStageAddresses(String accessToken, String code) throws JsonProcessingException;
}
