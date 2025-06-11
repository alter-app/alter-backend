package com.dreamteam.alter.adapter.inbound.general.reputation.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.reputation.dto.CreateReputationRequestDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "APP - 평판 관련 API")
public interface ReputationControllerSpec {

    ResponseEntity<CommonApiResponse<Void>> createReputationRequest(@Valid @RequestBody CreateReputationRequestDto request);

}
