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

    @Parameter(description = "업장 ID")
    private Long workspaceId;

    @Parameter(description = "대타 요청 상태 (ACCEPTED, REJECTED_BY_APPROVER, APPROVED만 조회 가능)")
    private SubstituteRequestStatus status;
}
