package com.dreamteam.alter.adapter.inbound.manager.file.controller;

import com.dreamteam.alter.adapter.inbound.common.dto.CommonApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.ErrorResponse;
import com.dreamteam.alter.adapter.inbound.manager.file.dto.ManagerGetPresignedUrlResponseDto;
import com.dreamteam.alter.adapter.inbound.manager.file.dto.ManagerUploadFileResponseDto;
import com.dreamteam.alter.domain.file.type.BucketType;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;


@Tag(name = "MANAGER - 파일 API")
public interface ManagerFileControllerSpec {

    @Operation(summary = "파일 업로드", description = "파일을 S3에 업로드하고 PENDING 상태로 저장합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "파일 업로드 성공"),
        @ApiResponse(responseCode = "400", description = "실패 케이스",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "유효하지 않은 파일", value = "{\"code\" : \"B022\"}"),
                    @ExampleObject(name = "허용되지 않는 파일 형식", value = "{\"code\" : \"B023\"}"),
                    @ExampleObject(name = "파일 크기 초과", value = "{\"code\" : \"B024\"}")
                }))
    })
    ResponseEntity<CommonApiResponse<ManagerUploadFileResponseDto>> uploadFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam("targetType") FileTargetType targetType,
        @RequestParam("bucketType") BucketType bucketType
    );

    @Operation(summary = "Presigned URL 조회", description = "본인이 업로드한 Private 파일 접근을 위한 Presigned URL을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Presigned URL 조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "본인 파일 아님", value = "{\"code\" : \"A005\"}")
                })),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 파일",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "파일 없음", value = "{\"code\" : \"B021\"}")
                }))
    })
    ResponseEntity<CommonApiResponse<ManagerGetPresignedUrlResponseDto>> getPresignedUrl(
        @PathVariable String fileId
    );

    @Operation(summary = "파일 삭제", description = "본인이 업로드한 파일을 S3에서 삭제하고 DELETED 상태로 변경합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "파일 삭제 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "본인 파일 아님", value = "{\"code\" : \"A005\"}")
                })),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 파일",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "파일 없음", value = "{\"code\" : \"B021\"}")
                }))
    })
    ResponseEntity<CommonApiResponse<Void>> deleteFile(
        @PathVariable String fileId
    );
}
