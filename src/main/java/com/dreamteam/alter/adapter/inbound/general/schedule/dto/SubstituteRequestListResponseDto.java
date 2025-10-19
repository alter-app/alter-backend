package com.dreamteam.alter.adapter.inbound.general.schedule.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.SentSubstituteRequestListResponse;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "대타 요청 목록 응답")
public class SubstituteRequestListResponseDto {

    @Schema(description = "대타 요청 ID")
    private Long id;

    @Schema(description = "스케줄 정보")
    private ScheduleInfo schedule;

    @Schema(description = "워크스페이스 정보")
    private WorkspaceInfo workspace;

    @Schema(description = "요청 타입")
    private DescribedEnumDto<SubstituteRequestType> requestType;

    @Schema(description = "요청 상태")
    private DescribedEnumDto<SubstituteRequestStatus> status;

    @Schema(description = "생성일시", example = "2024-01-15T10:00:00")
    private LocalDateTime createdAt;

    public static SubstituteRequestListResponseDto of(SentSubstituteRequestListResponse response) {
        return SubstituteRequestListResponseDto.builder()
            .id(response.getId())
            .schedule(ScheduleInfo.of(
                response.getScheduleId(),
                response.getScheduleStartDateTime(),
                response.getScheduleEndDateTime(),
                response.getPosition()
            ))
            .workspace(WorkspaceInfo.of(
                response.getWorkspaceId(),
                response.getWorkspaceName()
            ))
            .requestType(DescribedEnumDto.of(response.getRequestType(), SubstituteRequestType.describe()))
            .status(DescribedEnumDto.of(response.getStatus(), SubstituteRequestStatus.describe()))
            .createdAt(response.getCreatedAt())
            .build();
    }
}
