package com.dreamteam.alter.adapter.inbound.manager.workspace.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.SendWorkspaceInvitationRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceJoinRequestListFilterDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceJoinRequestResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "MANAGER - 업장 초대/합류 요청 관리 API")
public interface ManagerWorkspaceInvitationControllerSpec {

    @Operation(
        summary = "매니저 - 직원 초대 발송",
        description = """
            여러 휴대폰 번호로 사용자들을 업장에 초대합니다.

            **All-or-Nothing 처리:**
            - 모든 번호가 발송 가능한 경우에만 초대가 일괄 발송됩니다.
            - 발송 불가 번호(미가입, 이미 근무중, 이미 초대중)가 1개라도 있으면 에러를 반환하며, 에러 응답의 data에 발송 불가 번호 목록이 포함됩니다.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "초대 발송 완료"),
        @ApiResponse(responseCode = "400", description = "존재하지 않는 업장 (B008) | phoneNumbers가 비어 있음 | 발송 불가 번호 포함 (B001, data: 발송 불가 번호 목록)"),
        @ApiResponse(responseCode = "403", description = "해당 업장의 관리자가 아님 (A002)")
    })
    ResponseEntity<CommonApiResponse<Void>> sendInvitation(
        @PathVariable Long workspaceId,
        @Valid @RequestBody SendWorkspaceInvitationRequestDto request
    );

    @Operation(summary = "매니저 - 업장 합류 요청 목록 조회", description = """
        업장에 들어온 합류 요청 목록을 커서 기반 페이지네이션으로 조회합니다.

        - `status` 필터 미입력 시 전체 상태 조회 (PENDING, APPROVED, REJECTED)
        - `cursor` 미입력 시 첫 페이지 조회
        - 응답의 `page.cursor`를 다음 요청의 `cursor`로 사용
        """)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "합류 요청 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "존재하지 않는 업장 (B008)"),
        @ApiResponse(responseCode = "403", description = "해당 업장의 관리자가 아님 (A002)")
    })
    ResponseEntity<CursorPaginatedApiResponse<WorkspaceJoinRequestResponseDto>> getJoinRequestList(
        @PathVariable Long workspaceId,
        CursorPageRequestDto cursorPageRequest,
        WorkspaceJoinRequestListFilterDto filter
    );

    @Operation(summary = "매니저 - 합류 요청 승인", description = "합류 요청을 승인하고 해당 사용자를 직원으로 등록합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "합류 요청 승인 성공"),
        @ApiResponse(responseCode = "400", description = "존재하지 않는 업장 (B008) | 이미 근무 중인 사용자 (B018)"),
        @ApiResponse(responseCode = "403", description = "해당 업장의 관리자가 아님 (A002) | 해당 업장의 합류 요청이 아님 (A002)"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 합류 요청 (B019)"),
        @ApiResponse(responseCode = "409", description = "승인할 수 없는 상태의 합류 요청 (B020)")
    })
    ResponseEntity<CommonApiResponse<Void>> approveJoinRequest(
        @PathVariable Long workspaceId,
        @PathVariable Long requestId
    );

    @Operation(summary = "매니저 - 합류 요청 거절", description = "합류 요청을 거절합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "합류 요청 거절 성공"),
        @ApiResponse(responseCode = "400", description = "존재하지 않는 업장 (B008)"),
        @ApiResponse(responseCode = "403", description = "해당 업장의 관리자가 아님 (A002) | 해당 업장의 합류 요청이 아님 (A002)"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 합류 요청 (B019)"),
        @ApiResponse(responseCode = "409", description = "거절할 수 없는 상태의 합류 요청 (B020)")
    })
    ResponseEntity<CommonApiResponse<Void>> rejectJoinRequest(
        @PathVariable Long workspaceId,
        @PathVariable Long requestId
    );
}
