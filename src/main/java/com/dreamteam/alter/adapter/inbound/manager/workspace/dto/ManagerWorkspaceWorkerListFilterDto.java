package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
@Schema(description = "업장 관리자 근무자 목록 필터 DTO")
public class ManagerWorkspaceWorkerListFilterDto {

    @Schema(description = "근무자 상태 (eq)", example = "ACTIVATED / RESIGNED")
    private WorkspaceWorkerStatus status;

    @Schema(description = "근무자 이름 (like)", example = "홍길동")
    private String name;

    @Schema(description = "채용일 시작일 (gte)", example = "2023-01-01")
    private LocalDate employedAtFrom;

    @Schema(description = "채용일 종료일 (lte)", example = "2023-12-31")
    private LocalDate employedAtTo;

    @Schema(description = "퇴사일 시작일 (gte)", example = "2023-01-01")
    private LocalDate resignedAtFrom;

    @Schema(description = "퇴사일 종료일 (lte)", example = "2023-12-31")
    private LocalDate resignedAtTo;

}
