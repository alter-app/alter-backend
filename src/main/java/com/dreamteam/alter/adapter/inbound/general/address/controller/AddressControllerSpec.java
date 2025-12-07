package com.dreamteam.alter.adapter.inbound.general.address.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.general.address.dto.StageAddressRequestDto;
import com.dreamteam.alter.adapter.inbound.general.address.dto.StageAddressResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "APP - 주소 관련 API")
public interface AddressControllerSpec {

    @Operation(summary = "행정구역 주소 조회", description = "단계별 행정구역 주소를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "행정구역 주소 조회 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "잘못된 요청 (행정구역 코드가 5자리를 초과하는 경우)",
                        value = "{\"code\" : \"B001\"}"
                    ),
                    @ExampleObject(
                        name = "외부 API 연동 실패",
                        value = "{\"code\" : \"C002\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<StageAddressResponseDto>> getStageAddresses(
        StageAddressRequestDto request
    );
}
