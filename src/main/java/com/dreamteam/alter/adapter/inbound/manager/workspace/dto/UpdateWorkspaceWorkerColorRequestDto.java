package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매니저 - 근무자 색상 변경 요청 DTO")
public class UpdateWorkspaceWorkerColorRequestDto {

    @NotBlank
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상은 #로 시작하는 6자리 16진수여야 합니다.")
    @Schema(description = "변경할 색상 (hex)", example = "#93B0A4")
    private String colorCode;
}
