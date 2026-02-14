package com.dreamteam.alter.adapter.inbound.general.user.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.general.email.dto.SendEmailVerificationCodeRequestDto;
import com.dreamteam.alter.adapter.inbound.general.email.dto.VerifyEmailVerificationCodeRequestDto;
import com.dreamteam.alter.adapter.inbound.general.email.dto.VerifyEmailVerificationCodeResponseDto;
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
                    ),
                    @ExampleObject(
                        name = "비밀번호 형식 오류",
                        value = "{\"success\": false, \"code\" : \"A014\", \"message\" : \"비밀번호는 8~16자 이내 영문, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다.\"}"
                    ),
                    @ExampleObject(
                        name = "이메일 인증 세션 오류",
                        value = "{\"success\": false, \"code\" : \"A015\", \"message\" : \"이메일 인증 세션이 유효하지 않거나 만료되었습니다.\" }"
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
                    ),
                    @ExampleObject(
                        name = "비밀번호 형식 오류",
                        value = "{\"success\": false, \"code\" : \"A014\", \"message\" : \"비밀번호는 8~16자 이내 영문, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다.\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> resetPassword(@Valid ResetPasswordRequestDto request);

    @Operation(
            summary = "이메일 인증 코드 발송",
            description = "이메일로 6자리 인증 코드 발송"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "인증 코드 발송 성공"
            ),
            @ApiResponse(responseCode = "429", description = "요청이 너무 많음 (쿨다운)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "쿨다운 위반",
                                            value = "{\"success\": false, \"code\" : \"E004\", \"message\" : \"요청이 너무 많습니다. 잠시 후 다시 시도해주세요.\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 에러",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "이메일 전송 실패",
                                            value = "{\"success\": false, \"code\" : \"E003\", \"message\" : \"이메일 전송에 실패했습니다.\"}"
                                    )
                            }
                    )
            )
    })
    ResponseEntity<CommonApiResponse<Void>> sendVerificationCode(@Valid SendEmailVerificationCodeRequestDto request);

    @Operation(summary = "이메일 인증 코드 검증", description = "발송된 인증 코드를 검증합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 코드 검증 성공"),
            @ApiResponse(responseCode = "400", description = "실패 케이스",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "인증 코드 만료/없음",
                                            value = "{\"success\": false, \"code\" : \"E001\", \"message\" : \"인증 코드가 없거나 만료되었습니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "인증 코드 불일치",
                                            value = "{\"success\": false, \"code\" : \"E002\", \"message\" : \"인증 코드가 일치하지 않습니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "인증 시도 횟수 초과",
                                            value = "{\"success\": false, \"code\" : \"E005\", \"message\" : \"인증 시도 횟수를 초과했습니다. 코드를 다시 발송해주세요.\"}"
                                    )
                            }
                    )
            )
    })
    ResponseEntity<CommonApiResponse<VerifyEmailVerificationCodeResponseDto>> verifyVerificationCode(@Valid VerifyEmailVerificationCodeRequestDto request);


}
