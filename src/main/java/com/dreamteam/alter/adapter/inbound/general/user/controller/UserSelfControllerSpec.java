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
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "마이페이지 사용자 정보")
public interface UserSelfControllerSpec {

    @Operation(summary = "사용자 정보 조회")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "사용자 조회 실패",
                        value = "{\"code\" : \"B011\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<UserSelfInfoResponseDto>> getUserSelfInfo();

    @Operation(summary = "사용자 자격 정보 등록")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 자격 정보 등록 성공")
    })
    ResponseEntity<CommonApiResponse<Void>> addUserCertificate(@RequestBody @Valid CreateUserCertificateRequestDto request);

    @Operation(summary = "사용자 자격 정보 목록 조회")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 자격 정보 목록 조회 성공")
    })
    ResponseEntity<CommonApiResponse<List<UserSelfCertificateListResponseDto>>> getUserSelfCertificateList();

    @Operation(summary = "사용자 자격 정보 조회")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 자격 정보 조회 성공")
    })
    ResponseEntity<CommonApiResponse<UserSelfCertificateResponseDto>> getUserSelfCertificate(Long certificateId);

    @Operation(summary = "사용자 자격 정보 갱신")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 자격 정보 갱신 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "사용자 조회 실패",
                        value = "{\"code\" : \"B011\"}"
                    ),
                    @ExampleObject(
                        name = "사용자 자격 정보 조회 실패",
                        value = "{\"code\" : \"B014\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> updateUserSelfCertificate(
        Long certificateId,
        @RequestBody @Valid UpdateUserCertificateRequestDto request
    );

    @Operation(summary = "사용자 자격 정보 삭제")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 자격 정보 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "사용자 조회 실패",
                        value = "{\"code\" : \"B011\"}"
                    ),
                    @ExampleObject(
                        name = "사용자 자격 정보 조회 실패",
                        value = "{\"code\" : \"B014\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> deleteUserSelfCertificate(Long certificateId);

}
