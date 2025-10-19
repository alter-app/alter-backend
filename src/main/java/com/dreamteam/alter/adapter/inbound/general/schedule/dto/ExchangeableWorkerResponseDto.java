package com.dreamteam.alter.adapter.inbound.general.schedule.dto;

import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.UserWorkspaceWorkerListResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "교환 가능한 근무자 응답")
public class ExchangeableWorkerResponseDto {
    
    @Schema(description = "근무자 ID")
    private Long workerId;
    
    @Schema(description = "근무자 이름")
    private String workerName;

    public static ExchangeableWorkerResponseDto of(UserWorkspaceWorkerListResponse response) {
        return ExchangeableWorkerResponseDto.builder()
            .workerId(response.getId())
            .workerName(response.getUser().getName())
            .build();
    }
}
