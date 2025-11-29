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

    @Operation(summary = "회원가입 세션 생성")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원가입 세션 생성 성공")
    })
    ResponseEntity<CommonApiResponse<CreateSignupSessionResponseDto>> createSignupSession(@Valid CreateSignupSessionRequestDto request);

    @Operation(summary = "사용자 ID/PW 로그인")
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
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> loginWithPassword(@Valid LoginWithPasswordRequestDto request);

    @Operation(summary = "사용자 소셜 로그인")
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
                }))
    })
    ResponseEntity<CommonApiResponse<GenerateTokenResponseDto>> loginWithSocial(@Valid SocialLoginRequestDto request);

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

    @Operation(summary = "사용자 이메일 중복 체크")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이메일 중복 체크 성공")
    })
    ResponseEntity<CommonApiResponse<CheckEmailDuplicationResponseDto>> checkEmailDuplication(@Valid CheckEmailDuplicationRequestDto request);

    @Operation(
        summary = "이메일 찾기",
        description = "전화번호를 입력받아 해당하는 사용자의 마스킹된 이메일을 반환합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "이메일 찾기 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FindEmailResponseDto.class),
                examples = {
                    @ExampleObject(
                        name = "성공 응답",
                        value = "{\"success\": true, \"data\": {\"maskedEmail\": \"us**@example.com\"}}"
                    )
                }
            )
        ),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 사용자",
                        value = "{\"success\": false, \"code\" : \"B011\", \"message\" : \"존재하지 않는 사용자입니다.\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<FindEmailResponseDto>> findEmailByContact(@Valid FindEmailRequestDto request);

    @Operation(
        summary = "비밀번호 재설정 세션 생성",
        description = "이메일과 전화번호를 입력받아 사용자 유효성을 확인한 후, 비밀번호 재설정 세션을 생성합니다. 세션은 5분간 유효합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "비밀번호 재설정 세션 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CreatePasswordResetSessionResponseDto.class),
                examples = {
                    @ExampleObject(
                        name = "성공 응답",
                        value = "{\"success\": true, \"data\": {\"sessionId\": \"550e8400-e29b-41d4-a716-446655440000\"}}"
                    )
                }
            )
        ),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 사용자",
                        value = "{\"success\": false, \"code\" : \"B011\", \"message\" : \"존재하지 않는 사용자입니다.\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<CreatePasswordResetSessionResponseDto>> createPasswordResetSession(@Valid CreatePasswordResetSessionRequestDto request);

    @Operation(
        summary = "비밀번호 재설정",
        description = "비밀번호 재설정 세션 ID와 새로운 비밀번호를 입력받아 사용자의 비밀번호를 재설정합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "비밀번호 재설정 성공",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "성공 응답",
                        value = "{\"success\": true, \"data\": null}"
                    )
                }
            )
        ),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "비밀번호 재설정 세션이 존재하지 않거나 만료됨",
                        value = "{\"success\": false, \"code\" : \"A013\", \"message\" : \"비밀번호 재설정 세션이 존재하지 않거나 만료되었습니다.\"}"
                    ),
                    @ExampleObject(
                        name = "존재하지 않는 사용자",
                        value = "{\"success\": false, \"code\" : \"B011\", \"message\" : \"존재하지 않는 사용자입니다.\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> resetPassword(@Valid ResetPasswordRequestDto request);

}
