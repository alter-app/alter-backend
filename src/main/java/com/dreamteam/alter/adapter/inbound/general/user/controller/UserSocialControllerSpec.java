package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.SocialAccountStatusResponseDto;
import com.dreamteam.alter.adapter.inbound.general.user.dto.LinkSocialAccountRequestDto;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

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

    @Operation(summary = "소셜 계정 연동 해제")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "소셜 계정 연동 해제 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "연동되지 않은 소셜 플랫폼",
                        value = "{\"code\": \"A015\", \"message\": \"연동되지 않은 소셜 플랫폼입니다.\"}"
                    ),
                    @ExampleObject(
                        name = "마지막 소셜 계정 해제 불가",
                        value = "{\"code\": \"A016\", \"message\": \"비밀번호가 설정되지 않은 경우 마지막 소셜 계정은 해제할 수 없습니다.\"}"
                    )
                }
            ))
    })
    ResponseEntity<CommonApiResponse<Void>> unlinkSocialAccount(@PathVariable SocialProvider provider);

    @Operation(summary = "소셜 계정 연동 상태 조회")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "소셜 계정 연동 상태 조회 성공")
    })
    ResponseEntity<CommonApiResponse<List<SocialAccountStatusResponseDto>>> getLinkedSocialAccounts();
}
