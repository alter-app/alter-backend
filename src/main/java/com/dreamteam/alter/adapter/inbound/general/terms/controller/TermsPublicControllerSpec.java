package com.dreamteam.alter.adapter.inbound.general.terms.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.general.terms.dto.PublishedTermsItemResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Public - 약관")
public interface TermsPublicControllerSpec {

    @Operation(summary = "게시된 약관 목록 조회")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "약관 목록 조회 성공")
    })
    ResponseEntity<CommonApiResponse<List<PublishedTermsItemResponseDto>>> getPublishedTermsList();
}
