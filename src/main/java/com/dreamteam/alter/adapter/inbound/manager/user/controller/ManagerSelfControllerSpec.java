package com.dreamteam.alter.adapter.inbound.manager.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.manager.email.dto.SendEmailVerificationCodeRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.email.dto.VerifyEmailVerificationCodeRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.email.dto.VerifyEmailVerificationCodeResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.user.dto.ManagerSelfInfoResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.user.dto.RegisterEmailRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.user.dto.UpdateManagerProfileImageRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.user.dto.UpdateNicknameRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.user.dto.UpdatePasswordRequestDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "MANAGER - 마이페이지")
public interface ManagerSelfControllerSpec {

    @Operation(summary = "매니저 자신의 정보 조회")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "매니저 정보 조회 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "매니저 조회 실패",
                        value = "{\"code\" : \"B011\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<ManagerSelfInfoResponseDto>> getManagerSelfInfo();

    @Operation(
        summary = "이메일 등록/갱신",
        description = "이메일 인증 완료 후 발급된 세션 ID로 본인 계정의 이메일을 등록 또는 변경합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이메일 등록/갱신 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "이메일 인증 세션 유효하지 않음",
                        value = "{\"code\" : \"B001\", \"message\" : \"이메일 인증 세션이 유효하지 않거나 만료되었습니다.\"}"
                    ),
                    @ExampleObject(
                        name = "현재 이메일과 동일",
                        value = "{\"code\" : \"B001\", \"message\" : \"현재 등록된 이메일과 동일합니다.\"}"
                    ),
                    @ExampleObject(
                        name = "이메일 중복",
                        value = "{\"code\" : \"A004\"}"
                    ),
                    @ExampleObject(
                        name = "사용자 조회 실패",
                        value = "{\"code\" : \"B011\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> updateEmail(@RequestBody @Valid RegisterEmailRequestDto request);

    @Operation(
        summary = "이메일 삭제",
        description = "본인 계정에 등록된 이메일을 삭제합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이메일 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "이메일 미등록 사용자",
                        value = "{\"code\" : \"B001\", \"message\" : \"이메일이 등록되지 않은 사용자입니다.\"}"
                    ),
                    @ExampleObject(
                        name = "사용자 조회 실패",
                        value = "{\"code\" : \"B011\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> removeEmail();

    @Operation(
        summary = "이메일 인증 코드 발송",
        description = "이메일로 6자리 인증 코드 발송 (로그인 필요)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "인증 코드 발송 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "이메일 중복",
                        value = "{\"code\" : \"A004\", \"message\" : \"이미 가입된 이메일입니다.\"}"
                    )
                }
            )
        ),
        @ApiResponse(responseCode = "429", description = "쿨다운",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "쿨다운 위반",
                        value = "{\"code\" : \"E001\", \"message\" : \"요청이 너무 많습니다. 잠시 후 다시 시도해주세요.\"}"
                    )
                }
            )
        )
    })
    ResponseEntity<CommonApiResponse<Void>> sendVerificationCode(@RequestBody @Valid SendEmailVerificationCodeRequestDto request);

    @Operation(summary = "이메일 인증 코드 검증", description = "발송된 인증 코드를 검증합니다. (로그인 필요)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "인증 코드 검증 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "인증 코드 만료/없음",
                        value = "{\"code\" : \"B001\", \"message\" : \"인증 코드가 없거나 만료되었습니다.\"}"
                    ),
                    @ExampleObject(
                        name = "인증 코드 불일치",
                        value = "{\"code\" : \"B001\", \"message\" : \"인증 코드가 일치하지 않습니다.\"}"
                    ),
                    @ExampleObject(
                        name = "인증 시도 횟수 초과",
                        value = "{\"code\" : \"B001\", \"message\" : \"인증 시도 횟수를 초과했습니다.\"}"
                    )
                }
            )
        )
    })
    ResponseEntity<CommonApiResponse<VerifyEmailVerificationCodeResponseDto>> verifyVerificationCode(@RequestBody @Valid VerifyEmailVerificationCodeRequestDto request);

    @Operation(summary = "비밀번호 변경", description = "비밀번호가 설정된 사용자는 currentPassword 필수. 소셜 전용 사용자는 생략 가능.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "현재 비밀번호 불일치",
                        value = "{\"code\": \"A017\", \"message\": \"현재 비밀번호가 올바르지 않습니다.\"}"
                    ),
                    @ExampleObject(
                        name = "현재 비밀번호와 동일",
                        value = "{\"code\": \"B001\", \"message\": \"새 비밀번호는 현재 비밀번호와 달라야 합니다.\"}"
                    ),
                    @ExampleObject(
                        name = "비밀번호 형식 오류",
                        value = "{\"code\": \"A014\", \"message\": \"비밀번호는 8~16자 이내 영문, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다.\"}"
                    )
                }
            ))
    })
    ResponseEntity<CommonApiResponse<Void>> updatePassword(@RequestBody @Valid UpdatePasswordRequestDto request);

    @Operation(summary = "닉네임 변경")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "닉네임 변경 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "현재 닉네임과 동일",
                        value = "{\"code\": \"B001\", \"message\": \"현재 닉네임과 동일합니다.\"}"
                    ),
                    @ExampleObject(
                        name = "닉네임 중복",
                        value = "{\"code\": \"A008\"}"
                    )
                }
            ))
    })
    ResponseEntity<CommonApiResponse<Void>> updateNickname(@RequestBody @Valid UpdateNicknameRequestDto request);

    @Operation(
        summary = "프로필 이미지 수정",
        description = "기존 프로필 이미지를 삭제 상태로 변경하고, 본인이 업로드한 USER_PROFILE 파일을 새 프로필 이미지로 연결합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로필 이미지 수정 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "유효하지 않은 파일",
                        value = "{\"code\" : \"B022\"}"
                    )
                })),
        @ApiResponse(responseCode = "403", description = "권한 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "본인 파일 아님",
                        value = "{\"code\" : \"A002\"}"
                    )
                })),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 파일",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "파일 없음 또는 기존 프로필 이미지 없음",
                        value = "{\"code\" : \"B021\"}"
                    )
                })),
        @ApiResponse(responseCode = "409", description = "이미 연결된 파일",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "파일이 이미 연결됨",
                        value = "{\"code\" : \"B025\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> updateProfileImage(@RequestBody @Valid UpdateManagerProfileImageRequestDto request);

    @Operation(
        summary = "프로필 이미지 삭제",
        description = "본인 계정의 현재 프로필 이미지를 삭제 상태로 변경합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로필 이미지 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 파일",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "현재 연결된 프로필 이미지 없음",
                        value = "{\"code\" : \"B021\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> deleteProfileImage();

}
