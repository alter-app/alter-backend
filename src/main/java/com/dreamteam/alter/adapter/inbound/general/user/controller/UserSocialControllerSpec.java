package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.LinkSocialAccountRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "사용자 - 소셜 계정 연동")
public interface UserSocialControllerSpec {

    @Operation(summary = "소셜 계정 연동")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "소셜 계정 연동 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "이미 연동된 소셜 플랫폼",
                        value = "{\"code\" : \"A012\", \"message\" : \"이미 연동되어 있는 소셜 플랫폼입니다\"}"
                    ),
                    @ExampleObject(
                        name = "다른 사용자가 사용 중인 소셜 계정",
                        value = "{\"code\" : \"A005\", \"message\" : \"이미 가입된 소셜 계정입니다\"}"
                    ),
                    @ExampleObject(
                        name = "소셜 토큰 만료 (재 로그인 필요)",
                        value = "{\"code\" : \"A007\", \"message\" : \"소셜 토큰이 만료되었습니다\"}"
                    )
                }
            ))
    })
    ResponseEntity<CommonApiResponse<Void>> linkSocialAccount(@Valid LinkSocialAccountRequestDto request);
}
