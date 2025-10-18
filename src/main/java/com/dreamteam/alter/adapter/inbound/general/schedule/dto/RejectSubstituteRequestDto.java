package com.dreamteam.alter.adapter.inbound.general.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "대타 요청 거절 요청")
public class RejectSubstituteRequestDto {

    @Size(max = 500)
    @NotBlank
    @Schema(description = "거절 사유")
    private String targetRejectionReason;
}
