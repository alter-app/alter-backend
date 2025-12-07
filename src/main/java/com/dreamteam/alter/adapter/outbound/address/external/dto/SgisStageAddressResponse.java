package com.dreamteam.alter.adapter.outbound.address.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class SgisStageAddressResponse {

    @JsonProperty("result")
    private List<SgisStageAddressItem> result;

    @JsonProperty("errCd")
    private int errCd;

    @JsonProperty("errMsg")
    private String errMsg;
}
