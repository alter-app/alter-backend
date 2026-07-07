package com.dreamteam.alter.adapter.inbound.admin.posting.controller;

import com.dreamteam.alter.adapter.inbound.admin.posting.dto.AdminPostingKeywordRequestDto;
import com.dreamteam.alter.adapter.inbound.admin.posting.dto.AdminPostingKeywordResponseDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "ADMIN - 업종(키워드) 관리 API")
public interface AdminPostingKeywordControllerSpec {

    @Operation(summary = "업종 목록 조회", description = "관리자가 등록된 업종(키워드) 전체 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업종 목록 조회 성공")
    })
    ResponseEntity<CommonApiResponse<List<AdminPostingKeywordResponseDto>>> getPostingKeywordList();

    @Operation(summary = "업종 생성", description = "관리자가 새 업종(키워드)을 등록합니다. 이름은 중복될 수 없습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "업종 생성 성공"),
        @ApiResponse(responseCode = "400", description = "이름 중복 또는 유효성 검증 실패")
    })
    ResponseEntity<CommonApiResponse<AdminPostingKeywordResponseDto>> createPostingKeyword(
        @Valid @RequestBody AdminPostingKeywordRequestDto request
    );

    @Operation(summary = "업종 수정", description = "관리자가 업종(키워드)의 이름/설명을 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업종 수정 성공"),
        @ApiResponse(responseCode = "400", description = "이름 중복 또는 존재하지 않는 업종")
    })
    ResponseEntity<CommonApiResponse<Void>> updatePostingKeyword(
        @PathVariable Long id,
        @Valid @RequestBody AdminPostingKeywordRequestDto request
    );

    @Operation(summary = "업종 삭제", description = "관리자가 업종(키워드)을 삭제합니다. 공고에서 사용 중인 업종은 삭제할 수 없습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업종 삭제 성공"),
        @ApiResponse(responseCode = "409", description = "공고에서 사용 중인 업종")
    })
    ResponseEntity<CommonApiResponse<Void>> deletePostingKeyword(
        @PathVariable Long id
    );
}
