package com.dreamteam.alter.adapter.inbound.manager.schedule.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.dreamteam.alter.domain.workspace.model.WorkspaceShiftTodayResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "매니저 금일 근무자 응답")
public class ManagerTodayScheduleResponseDto {

    @Schema(description = "근무자 ID", example = "1")
    private Long workerId;

    @Schema(description = "근무자 이름", example = "홍길동")
    private String workerName;

    @Schema(description = "근무자 프로필 이미지 S3 URL")
    private String profileImageUrl;

    @Schema(description = "금일 근무 목록")
    private List<ManagerTodayScheduleShiftItem> shifts;

    public static List<ManagerTodayScheduleResponseDto> from(List<WorkspaceShiftTodayResponse> responses) {
        return responses.stream()
            .collect(Collectors.groupingBy(WorkspaceShiftTodayResponse::workerId))
            .values()
            .stream()
            .map(group -> {
                WorkspaceShiftTodayResponse first = group.getFirst();
                return ManagerTodayScheduleResponseDto.builder()
                    .workerId(first.workerId())
                    .workerName(first.workerName())
                    .profileImageUrl(first.profileImageUrl())
                    .shifts(
                        group.stream()
                            .map(r -> ManagerTodayScheduleShiftItem.of(r.shiftId(), r.startDateTime(), r.endDateTime()))
                            .toList()
                    )
                    .build();
            })
            .toList();
    }
}
