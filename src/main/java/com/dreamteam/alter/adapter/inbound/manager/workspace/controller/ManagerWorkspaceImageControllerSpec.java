package com.dreamteam.alter.adapter.inbound.manager.workspace.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.UpdateWorkspaceImagesRequestDto;
import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.WorkspaceImageResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "MANAGER - 업장 대표이미지 API")
public interface ManagerWorkspaceImageControllerSpec {

    @Operation(summary = "매니저 - 업장 대표이미지 목록 조회", description = "노출 순서(sortOrder) 오름차순으로 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "대표이미지 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "존재하지 않는 업장",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 업장입니다.",
                        value = "{\"code\" : \"B008\"}"
                    ),
                })),
    })
    ResponseEntity<CommonApiResponse<List<WorkspaceImageResponseDto>>> getWorkspaceImages(
        @PathVariable Long workspaceId
    );

    @Operation(summary = "매니저 - 업장 대표이미지 수정 (전체 교체)", description = "전달한 파일 ID 목록으로 대표이미지를 전체 교체합니다. 목록 순서가 노출 순서이며, 추가/삭제/순서변경을 한 번에 처리합니다. 최대 5개.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "대표이미지 수정 성공"),
        @ApiResponse(responseCode = "400", description = "400 Error 실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "존재하지 않는 업장입니다.",
                        value = "{\"code\" : \"B008\"}"
                    ),
                    @ExampleObject(
                        name = "대표이미지는 최대 5개까지 등록할 수 있습니다.",
                        value = "{\"code\" : \"B026\"}"
                    ),
                    @ExampleObject(
                        name = "존재하지 않는 파일입니다.",
                        value = "{\"code\" : \"B021\"}"
                    ),
                    @ExampleObject(
                        name = "유효하지 않은 파일입니다.",
                        value = "{\"code\" : \"B022\"}"
                    ),
                })),
        @ApiResponse(responseCode = "409", description = "이미 연결된 파일",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "이미 연결된 파일입니다.",
                        value = "{\"code\" : \"B025\"}"
                    ),
                })),
    })
    ResponseEntity<CommonApiResponse<Void>> updateWorkspaceImages(
        @PathVariable Long workspaceId,
        @RequestBody @Valid UpdateWorkspaceImagesRequestDto request
    );
}
