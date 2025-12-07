package com.dreamteam.alter.adapter.inbound.general.address.dto;

import com.dreamteam.alter.domain.address.model.SgisStageAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SGIS 단계별 주소 항목")
public class StageAddressItemResponseDto {

    @Schema(description = "행정구역 코드")
    private String code;

    @Schema(description = "행정구역 명")
    private String name;

    public static StageAddressItemResponseDto from(SgisStageAddress address) {
        return new StageAddressItemResponseDto(
            address.getCode(),
            address.getName()
        );
    }
}
