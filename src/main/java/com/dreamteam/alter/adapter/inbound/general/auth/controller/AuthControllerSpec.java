package com.dreamteam.alter.adapter.inbound.general.auth.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.FirebaseCustomTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.GenerateTokenResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@Tag(name = "APP - 인증 관련 API")
public interface AuthControllerSpec {

    @Operation(summary = "토큰 재발급", description = "헤더에 RefreshToken을 담아 요청")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "재발급 성공 (JWT 응답)"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "RefreshToken을 통한 요청이 아닐 경우",
                        value = "{\"code\" : \"B002\"}"
                    ),
                    @ExampleObject(
                        name = "이용 정지된 사용자",
                        value = "{\"code\" : \"B003\"}"
                    ),
                    @ExampleObject(
                        name = "탈퇴한 사용자",
                        value = "{\"code\" : \"B004\"}"
                    ),
                    @ExampleObject(
                        name = "서버 내부 처리 오류",
                        value = "{\"code\" : \"C001\"}"
                    ),
                }))
    })
    ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> reissueToken(Authentication authentication);

    @Operation(summary = "로그아웃")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    ResponseEntity<CommonApiResponse<Void>> logout();

    @Operation(summary = "Firebase 커스텀 토큰 발급")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Firebase 커스텀 토큰 발급 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<CommonApiResponse<FirebaseCustomTokenResponseDto>> generateFirebaseCustomToken();

}
