package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import com.dreamteam.alter.domain.workspace.type.BusinessJoinRequestStatus;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
@Schema(description = "업장 합류 요청 목록 필터 DTO")
public class WorkspaceJoinRequestListFilterDto {

    @Parameter(description = "상태 필터 (PENDING | APPROVED | REJECTED), 미입력 시 전체 조회")
    private BusinessJoinRequestStatus status;

    @Parameter(description = "조회 시작일 (ISO: 2026-03-01), 미입력 시 제한 없음")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;

    @Parameter(description = "조회 종료일 (ISO: 2026-03-31, 해당일 포함), 미입력 시 제한 없음")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;
}
