package com.dreamteam.alter.adapter.outbound.address.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class SgisAuthResult {

    @JsonProperty("accessToken")
    private String accessToken;

    @JsonProperty("accessTimeout")
    private String accessTimeout;
}
