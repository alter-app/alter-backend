package com.dreamteam.alter.adapter.inbound.general.reputation.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.AvailableReputationKeywordResponseDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.WorkspaceCreateReputationRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "MANAGER - 업장 평판 관련 API")
public interface WorkspaceReputationControllerSpec {

    @Operation(summary = "평판 키워드 조회", description = "사용 가능한 평판 키워드를 조회합니다.")
    ResponseEntity<CommonApiResponse<AvailableReputationKeywordResponseDto>> getReputationKeywords();

    @Operation(summary = "알바생 평판 생성", description = "업장에 대한 알바생 평판을 생성합니다.")
    ResponseEntity<CommonApiResponse<Void>> createReputation(@Valid @RequestBody WorkspaceCreateReputationRequestDto request);

}
