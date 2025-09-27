package com.dreamteam.alter.adapter.inbound.manager.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "근무자 변경 요청")
public class UpdateWorkerRequestDto {

    @Schema(description = "새로운 근무자 ID", example = "1", required = true)
    @NotNull(message = "근무자 ID는 필수입니다")
    private Long workerId;
}
