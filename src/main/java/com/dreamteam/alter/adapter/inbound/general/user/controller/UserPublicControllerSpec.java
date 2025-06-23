package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.*;
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
                        value = "{\"code\": \"A003\", \"data\": {\"signupSessionId\": \"UUID\", \"name\": \"김철수\", \"gender\": \"GENDER_MALE\", \"birthday\": \"YYYYMMDD\"}}"
                    ),
                    @ExampleObject(
                        name = "소셜 토큰 만료 (재 로그인 필요)",
                        value = "{\"code\" : \"A007\"}"
                    ),
                    @ExampleObject(
                        name = "가입 되지 않은 사용자 - 사용자 Email 중복 (다른 소셜 계정으로 로그인 유도)",
                        value = "{\"code\" : \"A004\"}"
                    ),
                }))
    })
    ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> loginUser(@Valid LoginUserRequestDto request);

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
                        value = "{\"code\" : \"A004\"}"
                    ),
                    @ExampleObject(
                        name = "소셜 플랫폼 ID 중복",
                        value = "{\"code\" : \"A005\"}"
                    ),
                    @ExampleObject(
                        name = "사용자 닉네임 중복",
                        value = "{\"code\" : \"A008\"}"
                    ),
                    @ExampleObject(
                        name = "소셜 토큰 만료 (재 로그인 필요)",
                        value = "{\"code\" : \"A007\"}"
                    ),
                    @ExampleObject(
                        name = "회원 가입 세션이 존재하지 않음",
                        value = "{\"code\" : \"A006\"}"
                    ),
                    @ExampleObject(
                        name = "사용자 휴대폰 번호 중복",
                        value = "{\"code\" : \"A009\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> createUser(@Valid CreateUserRequestDto request);

    @Operation(summary = "사용자 닉네임 중복 체크")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "닉네임 중복 체크 성공")
    })
    ResponseEntity<CommonApiResponse<CheckNicknameDuplicationResponseDto>> checkNicknameDuplication(@Valid CheckNicknameDuplicationRequestDto request);

    @Operation(summary = "사용자 휴대폰 번호 중복 체크")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "휴대폰 번호 중복 체크 성공")
    })
    ResponseEntity<CommonApiResponse<CheckContactDuplicationResponseDto>> checkContactDuplication(@Valid CheckContactDuplicationRequestDto request);

}
