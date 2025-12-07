package com.dreamteam.alter.adapter.outbound.address.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class SgisAuthResponse {

    @JsonProperty("result")
    private SgisAuthResult result;

    @JsonProperty("errCd")
    private int errCd;

    @JsonProperty("errMsg")
    private String errMsg;
}
