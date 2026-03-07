package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateFixedScheduleDateRequestDto(
	@NotNull
	@Min(1)
	@Max(31)
	Integer nextMonthShiftGenDay
) {}
