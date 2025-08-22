package com.dreamteam.alter.adapter.inbound.manager.reputation.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.AvailableReputationKeywordRequestDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.AvailableReputationKeywordResponseDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.CreateReputationToUserRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.reputation.ReputationRequestListResponseDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.ReputationRequestFilterDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "MANAGER - 업장 평판 관련 API")
public interface WorkspaceReputationControllerSpec {

    @Operation(summary = "평판 키워드 조회", description = "사용 가능한 평판 키워드를 조회합니다.")
    ResponseEntity<CommonApiResponse<AvailableReputationKeywordResponseDto>> getReputationKeywords(
        AvailableReputationKeywordRequestDto keywordRequestDto
    );

    @Operation(summary = "알바생 평판 생성", description = "업장에 대한 알바생 평판을 생성합니다.")
    ResponseEntity<CommonApiResponse<Void>> createReputationToUser(@Valid @RequestBody CreateReputationToUserRequestDto request);

    @Operation(summary = "업장 평판 요청 목록 조회", description = "업장에 대한 평판 요청 목록을 조회합니다.")
    ResponseEntity<CursorPaginatedApiResponse<ReputationRequestListResponseDto>> getReputationRequestList(
        ReputationRequestFilterDto filter,
        CursorPageRequestDto pageRequest
    );

    @Operation(summary = "업장 평판 요청 거절", description = "업장에 대한 평판 요청을 거절합니다.")
    ResponseEntity<CommonApiResponse<Void>> declineReputationRequest(@PathVariable Long requestId);

}
