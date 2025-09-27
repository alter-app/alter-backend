package com.dreamteam.alter.adapter.inbound.manager.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "스케줄 생성 요청")
public class CreateWorkScheduleRequestDto {
    
    @Schema(description = "워크스페이스 ID", example = "1")
    @NotNull(message = "업장 ID는 필수입니다")
    private Long workspaceId;
    
    @Schema(description = "근무 시작 시간", example = "2024-01-15T09:00:00")
    @NotNull(message = "시작 시간은 필수입니다")
    private LocalDateTime startDateTime;
    
    @Schema(description = "근무 종료 시간", example = "2024-01-15T18:00:00")
    @NotNull(message = "종료 시간은 필수입니다")
    private LocalDateTime endDateTime;
    
    @Schema(description = "직책", example = "바리스타")
    @NotBlank(message = "직책은 필수입니다")
    private String position;
}
