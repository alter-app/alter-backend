package com.dreamteam.alter.adapter.inbound.general.address.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springdoc.core.annotations.ParameterObject;

@Getter
@Setter
@NoArgsConstructor
@ParameterObject
@Schema(description = "SGIS 단계별 주소 조회 요청")
public class StageAddressRequestDto {

    @Parameter(description = "행정구역 코드 (없으면 시/도 전체)")
    private String code;
}
