package com.dreamteam.alter.adapter.inbound.general.reputation.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.AvailableReputationKeywordResponseDto;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.UserCreateReputationRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "APP - 사용자 평판 관련 API")
public interface UserReputationControllerSpec {

    @Operation(summary = "평판 키워드 조회", description = "사용 가능한 평판 키워드를 조회합니다.")
    ResponseEntity<CommonApiResponse<AvailableReputationKeywordResponseDto>> getReputationKeywords();

    @Operation(summary = "평판 생성", description = "상대방에 대한 평판을 키워드와 함께 생성합니다.")
    ResponseEntity<CommonApiResponse<Void>> createReputation(@Valid @RequestBody UserCreateReputationRequestDto request);

}
