package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매니저 - 고정근무 생성일 변경 DTO")
public class UpdateFixedScheduleDateRequestDto {

	@NotNull(message = "시작 요일은 필수입니다")
	@Min(value = 1, message = "1 보다 커야합니다.")
	@Max(value = 31, message = "31 보다 작아야합니다")
	@Schema(description = "변경일", example = "15")
	Integer nextMonthShiftGenDay;
}
