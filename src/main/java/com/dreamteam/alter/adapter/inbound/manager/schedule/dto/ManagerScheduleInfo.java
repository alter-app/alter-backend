package com.dreamteam.alter.adapter.inbound.manager.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "매니저용 스케줄 정보")
public class ManagerScheduleInfo {
    
    @Schema(description = "스케줄 ID", example = "1")
    private Long scheduleId;
    
    @Schema(description = "시작 시간", example = "2024-01-15T09:00:00")
    private LocalDateTime startDateTime;
    
    @Schema(description = "종료 시간", example = "2024-01-15T18:00:00")
    private LocalDateTime endDateTime;
    
    @Schema(description = "직책", example = "바리스타")
    private String position;

    public static ManagerScheduleInfo of(
        Long scheduleId,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        String position
    ) {
        return ManagerScheduleInfo.builder()
            .scheduleId(scheduleId)
            .startDateTime(startDateTime)
            .endDateTime(endDateTime)
            .position(position)
            .build();
    }

}
