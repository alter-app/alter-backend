package com.dreamteam.alter.adapter.inbound.manager.schedule.dto;

import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
@Schema(description = "매니저 대타 요청 목록 필터 DTO")
public class ManagerSubstituteRequestListFilterDto {

    @Parameter(description = "대타 요청 상태")
    @Schema(description = "대타 요청 상태 (ACCEPTED, APPROVED, REJECTED_BY_APPROVER, CANCELLED)")
    private SubstituteRequestStatus status;
}
