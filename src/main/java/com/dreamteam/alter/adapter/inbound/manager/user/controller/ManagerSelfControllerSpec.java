package com.dreamteam.alter.adapter.inbound.manager.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.manager.user.dto.CreateManagerProfileImageRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.user.dto.ManagerSelfInfoResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.user.dto.UpdateManagerProfileImageRequestDto;

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
        summary = "프로필 이미지 등록",
        description = "본인 계정에 프로필 이미지가 없을 때, 본인이 업로드한 USER_PROFILE 파일을 프로필 이미지로 연결합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로필 이미지 등록 성공"),
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
                        name = "파일 없음",
                        value = "{\"code\" : \"B021\"}"
                    )
                })),
        @ApiResponse(responseCode = "409", description = "이미 연결된 파일",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "이미 프로필 이미지가 있거나 파일이 연결됨",
                        value = "{\"code\" : \"B025\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> createProfileImage(@RequestBody @Valid CreateManagerProfileImageRequestDto request);

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

}
