package com.dreamteam.alter.adapter.inbound.general.address.dto;

import com.dreamteam.alter.domain.address.model.SgisStageAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "SGIS 단계별 주소 조회 응답")
public class StageAddressResponseDto {

    @Schema(description = "주소 목록")
    private List<StageAddressItemResponseDto> addresses;

    public static StageAddressResponseDto from(List<SgisStageAddress> addresses) {
        return StageAddressResponseDto.builder()
            .addresses(
                addresses.stream()
                    .map(StageAddressItemResponseDto::from)
                    .toList()
            )
            .build();
    }
}
