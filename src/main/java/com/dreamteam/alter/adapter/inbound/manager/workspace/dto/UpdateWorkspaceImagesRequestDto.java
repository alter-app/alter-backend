package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import java.util.Set;

import com.dreamteam.alter.adapter.inbound.common.dto.WorkspaceImageRequestDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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
    @Schema(description = "대표이미지 목록 (fileId 중복 자동 제거, sortOrder 오름차순으로 노출, 비우면 전체 삭제)")
    private Set<@Valid WorkspaceImageRequestDto> images;
}
