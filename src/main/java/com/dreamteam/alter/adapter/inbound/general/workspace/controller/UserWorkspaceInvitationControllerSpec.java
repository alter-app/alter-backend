package com.dreamteam.alter.adapter.inbound.general.workspace.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyInvitationResponseDto;
import com.dreamteam.alter.adapter.inbound.general.workspace.dto.MyJoinRequestResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "USER - 업장 초대/합류 요청 API")
public interface UserWorkspaceInvitationControllerSpec {

    @Operation(summary = "알바생 - 업장 합류 요청 보내기", description = "특정 업장에 합류 요청을 보냅니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "합류 요청 성공")
    })
    ResponseEntity<CommonApiResponse<Void>> sendJoinRequest(
        @PathVariable Long workspaceId
    );

    @Operation(summary = "알바생 - 내가 보낸 합류 요청 목록 조회", description = "내가 보낸 PENDING 상태의 합류 요청 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "합류 요청 목록 조회 성공")
    })
    ResponseEntity<CommonApiResponse<List<MyJoinRequestResponseDto>>> getMyJoinRequestList();

    @Operation(summary = "알바생 - 내가 받은 초대 목록 조회", description = "나에게 온 PENDING 상태의 업장 초대 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "초대 목록 조회 성공")
    })
    ResponseEntity<CommonApiResponse<List<MyInvitationResponseDto>>> getMyInvitationList();

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
