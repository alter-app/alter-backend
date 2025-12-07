package com.dreamteam.alter.adapter.outbound.address.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class SgisStageAddressItem {

    @JsonProperty("addr_name")
    private String addrName;

    @JsonProperty("cd")
    private String code;

    @JsonProperty("full_addr")
    private String fullAddr;

    @JsonProperty("y_coor")
    private String yCoor;

    @JsonProperty("x_coor")
    private String xCoor;
}
