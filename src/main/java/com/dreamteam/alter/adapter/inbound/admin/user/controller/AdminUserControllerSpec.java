package com.dreamteam.alter.adapter.inbound.admin.user.controller;

import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUpdateUserPasswordRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUpdateUserStatusRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUserDetailResponseDto;
import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUserListFilterDto;
import com.dreamteam.alter.adapter.inbound.admin.user.dto.AdminUserListResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.PageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.PaginatedResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "ADMIN - 관리자 회원 관리 API")
public interface AdminUserControllerSpec {

    @Operation(summary = "회원 목록 조회", description = "관리자가 회원 목록을 오프셋 페이징으로 조회합니다. 상태, 역할, 키워드로 필터링할 수 있습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "잘못된 요청 (유효하지 않은 페이지, 페이지 크기 등)",
                        value = "{\"code\" : \"B001\"}"
                    )
                }))
    })
    ResponseEntity<PaginatedResponseDto<AdminUserListResponseDto>> getUserList(
        PageRequestDto request,
        AdminUserListFilterDto filter
    );

    @Operation(summary = "회원 상세 조회", description = "관리자가 회원 상세 정보를 조회합니다. 평판 정보가 포함됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원 상세 조회 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 회원",
                        value = "{\"code\" : \"B011\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<AdminUserDetailResponseDto>> getUserDetail(
        @Parameter(description = "회원 ID", example = "1") @PathVariable Long userId
    );

    @Operation(summary = "회원 비밀번호 변경", description = "관리자가 회원 비밀번호를 변경합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원 비밀번호 변경 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 회원",
                        value = "{\"code\" : \"B011\"}"
                    ),
                    @ExampleObject(
                        name = "잘못된 비밀번호 형식",
                        value = "{\"code\" : \"B002\"}"
                    ),
                    @ExampleObject(
                        name = "잘못된 요청 (유효성 검증 실패)",
                        value = "{\"code\" : \"B001\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> updateUserPassword(
        @Parameter(description = "회원 ID", example = "1") @PathVariable Long userId,
        @Valid @RequestBody AdminUpdateUserPasswordRequestDto request
    );

    @Operation(summary = "회원 상태 변경", description = "관리자가 회원 상태를 변경합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원 상태 변경 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 회원",
                        value = "{\"code\" : \"B011\"}"
                    ),
                    @ExampleObject(
                        name = "잘못된 요청 (유효하지 않은 상태 값 등)",
                        value = "{\"code\" : \"B001\"}"
                    )
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> updateUserStatus(
        @Parameter(description = "회원 ID", example = "1") @PathVariable Long userId,
        @Valid @RequestBody AdminUpdateUserStatusRequestDto request
    );
}
