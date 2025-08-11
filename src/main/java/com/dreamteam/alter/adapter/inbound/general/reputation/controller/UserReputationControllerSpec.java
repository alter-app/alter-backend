package com.dreamteam.alter.adapter.inbound.general.reputation.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.UserCreateReputationRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "평판", description = "사용자 평판 관련 API")
public interface UserReputationControllerSpec {

    @Operation(summary = "평판 생성", description = "상대방에 대한 평판을 키워드와 함께 생성합니다.")
    ResponseEntity<CommonApiResponse<Void>> createReputation(@Valid @RequestBody UserCreateReputationRequestDto request);

}
