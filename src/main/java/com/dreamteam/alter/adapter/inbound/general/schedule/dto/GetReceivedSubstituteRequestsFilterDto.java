package com.dreamteam.alter.adapter.inbound.general.schedule.dto;

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
@Schema(description = "대타 요청 조회 필터 DTO")
public class GetReceivedSubstituteRequestsFilterDto {

    @Parameter(description = "업장 ID")
    private Long workspaceId;

    @Parameter(description = "대타 요청 상태 필터")
    private SubstituteRequestStatus status;

}
