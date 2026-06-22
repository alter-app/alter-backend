package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "업장 대표이미지 응답 DTO")
public class WorkspaceImageResponseDto {

    @Schema(description = "파일 ID", example = "01959b4e-4e5f-7c3a-8d9e-0f1a2b3c4d5e")
    private String fileId;

    @Schema(description = "이미지 URL", example = "https://cdn.example.com/workspace_representative_image/abc.jpg")
    private String url;

    @Schema(description = "노출 순서 (0부터 시작)", example = "0")
    private int sortOrder;

    public static WorkspaceImageResponseDto of(String fileId, String url, int sortOrder) {
        return WorkspaceImageResponseDto.builder()
            .fileId(fileId)
            .url(url)
            .sortOrder(sortOrder)
            .build();
    }
}
