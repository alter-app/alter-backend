package com.dreamteam.alter.adapter.inbound.general.user.dto;

import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.UserWorkspaceListResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "사용자 근무 업장 목록 응답")
public class UserWorkspaceListResponseDto {

    @Schema(description = "업장 ID", example = "1")
    private Long workspaceId;

    @Schema(description = "업장명", example = "스타벅스 강남점")
    private String businessName;

    @Schema(description = "입사일", example = "2024-01-15")
    private LocalDate employedAt;

    @Schema(description = "다음 근무 일정", example = "2024-01-20T09:00:00")
    private LocalDateTime nextShiftDateTime;

    public static UserWorkspaceListResponseDto from(UserWorkspaceListResponse response) {
        return UserWorkspaceListResponseDto.builder()
            .workspaceId(response.getWorkspaceId())
            .businessName(response.getBusinessName())
            .employedAt(response.getEmployedAt())
            .nextShiftDateTime(response.getNextShiftDateTime())
            .build();
    }
}
