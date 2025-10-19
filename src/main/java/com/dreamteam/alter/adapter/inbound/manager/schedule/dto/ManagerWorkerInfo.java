package com.dreamteam.alter.adapter.inbound.manager.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "매니저용 근무자 정보")
public class ManagerWorkerInfo {
    
    @Schema(description = "근무자 ID", example = "1")
    private Long workerId;
    
    @Schema(description = "근무자 이름", example = "홍길동")
    private String workerName;

    public static ManagerWorkerInfo of(Long workerId, String workerName) {
        return ManagerWorkerInfo.builder()
            .workerId(workerId)
            .workerName(workerName)
            .build();
    }

}
