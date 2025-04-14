package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.CreateUserRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.UserLoginRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "Public - 사용자")
public interface UserPublicControllerSpec {

    @Operation(summary = "사용자 소셜 로그인")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공 (JWT 응답)"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "가입되지 않은 사용자",
                        value = "{\"code\" : \"A003\"}")
                }))
    })
    ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> loginUser(@Valid UserLoginRequestDto request);

    @Operation(summary = "회원가입을 수행한다")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원 가입 및 로그인 성공 (JWT 응답)"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "이메일 중복",
                        value = "{\"code\" : \"A004\"}"),
                    @ExampleObject(
                        name = "소셜 플랫폼 ID 중복",
                        value = "{\"code\" : \"A005\"}")
                }))
    })
    ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> createUser(@Valid CreateUserRequestDto request);

}
