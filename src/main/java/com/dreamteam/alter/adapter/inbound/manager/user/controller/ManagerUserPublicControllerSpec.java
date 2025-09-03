package com.dreamteam.alter.adapter.inbound.manager.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.SocialLoginRequestDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.LoginWithPasswordRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Public - 업장 매니저")
public interface ManagerUserPublicControllerSpec {

    @Operation(summary = "업장 매니저 소셜 로그인")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공 (JWT 응답)"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 사용자 계정",
                        value = "{\"code\" : \"B011\"}"
                    ),
                    @ExampleObject(
                        name = "사용자 권한 없음",
                        value = "{\"code\" : \"A002\"}"
                    ),
                }))
    })
    ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> loginManagerUserSocial(@RequestBody @Valid SocialLoginRequestDto request);

    @Operation(summary = "업장 매니저 ID/PW 로그인")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공 (JWT 응답)"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "이메일 또는 비밀번호가 올바르지 않을 경우",
                        value = "{\"code\" : \"A011\", \"message\" : \"로그인 정보가 올바르지 않습니다\"}"
                    ),
                    @ExampleObject(
                        name = "사용자 권한 없음",
                        value = "{\"code\" : \"A002\", \"message\" : \"접근 권한이 없습니다\"}"
                    ),
                }))
    })
    ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> loginManagerUser(@RequestBody @Valid LoginWithPasswordRequestDto request);
}
