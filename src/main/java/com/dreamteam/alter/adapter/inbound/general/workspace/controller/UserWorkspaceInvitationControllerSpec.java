package com.dreamteam.alter.adapter.inbound.general.workspace.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyInvitationResponseDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyJoinRequestResponseDto;
import com.dreamteam.alter.domain.workspace.type.BusinessInvitationStatus;
import com.dreamteam.alter.domain.workspace.type.BusinessJoinRequestStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "USER - 업장 초대/합류 요청 API")
public interface UserWorkspaceInvitationControllerSpec {

    @Operation(summary = "알바생 - 업장 합류 요청 보내기", description = "특정 업장에 합류 요청을 보냅니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "합류 요청 성공")
    })
    ResponseEntity<CommonApiResponse<Void>> sendJoinRequest(
        @PathVariable Long workspaceId
    );

    @Operation(summary = "알바생 - 내가 보낸 합류 요청 목록 조회", description = """
        내가 보낸 합류 요청 목록을 커서 기반 페이지네이션으로 조회합니다.

        - `status` 필터 미입력 시 전체 상태 조회 (PENDING, APPROVED, REJECTED)
        - `cursor` 미입력 시 첫 페이지 조회
        - 응답의 `page.cursor`를 다음 요청의 `cursor`로 사용
        """)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "합류 요청 목록 조회 성공")
    })
    ResponseEntity<CursorPaginatedApiResponse<MyJoinRequestResponseDto>> getMyJoinRequestList(
        CursorPageRequestDto cursorPageRequest,
        @Parameter(description = "상태 필터 (PENDING | APPROVED | REJECTED), 미입력 시 전체 조회")
        @RequestParam(required = false) BusinessJoinRequestStatus status
    );

    @Operation(summary = "알바생 - 내가 받은 초대 목록 조회", description = """
        나에게 온 업장 초대 목록을 커서 기반 페이지네이션으로 조회합니다.

        - `status` 필터 미입력 시 전체 상태 조회 (PENDING, ACCEPTED, DECLINED, EXPIRED)
        - `cursor` 미입력 시 첫 페이지 조회
        - 응답의 `page.cursor`를 다음 요청의 `cursor`로 사용
        """)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "초대 목록 조회 성공")
    })
    ResponseEntity<CursorPaginatedApiResponse<MyInvitationResponseDto>> getMyInvitationList(
        CursorPageRequestDto cursorPageRequest,
        @Parameter(description = "상태 필터 (PENDING | ACCEPTED | DECLINED | EXPIRED), 미입력 시 전체 조회")
        @RequestParam(required = false) BusinessInvitationStatus status
    );

    @Operation(summary = "알바생 - 업장 초대 수락", description = "받은 업장 초대를 수락합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "초대 수락 성공")
    })
    ResponseEntity<CommonApiResponse<Void>> acceptInvitation(
        @PathVariable Long invitationId
    );

    @Operation(summary = "알바생 - 업장 초대 거절", description = "받은 업장 초대를 거절합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "초대 거절 성공")
    })
    ResponseEntity<CommonApiResponse<Void>> declineInvitation(
        @PathVariable Long invitationId
    );
}
