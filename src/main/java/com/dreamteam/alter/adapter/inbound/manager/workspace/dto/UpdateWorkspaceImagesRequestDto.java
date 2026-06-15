package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "업장 대표이미지 수정 요청 DTO (전체 교체)")
public class UpdateWorkspaceImagesRequestDto {

    @NotNull
    @Size(max = 5, message = "대표이미지는 최대 5개까지 등록할 수 있습니다.")
    @Schema(description = "대표이미지로 설정할 파일 ID 목록 (목록 순서가 노출 순서, 비우면 전체 삭제)", example = "[\"01959b4e-4e5f-7c3a-8d9e-0f1a2b3c4d5e\"]")
    private List<String> fileIds;
}
