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

    @Parameter(description = "대타 요청 상태 필터. 비우면 본인 기준으로 취소·만료됐거나 다른 근무자가 수락한 요청은 제외하고 조회하며(본인이 수락한 요청은 항상 포함), 값을 지정하면 해당 요청 상태만 조회")
    private SubstituteRequestStatus status;

}
