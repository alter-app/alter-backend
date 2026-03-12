package com.dreamteam.alter.adapter.inbound.general.workspace.dto;

import com.dreamteam.alter.domain.workspace.entity.BusinessJoinRequest;
import com.dreamteam.alter.domain.workspace.type.BusinessJoinRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "내가 보낸 합류 요청 응답 DTO")
public class MyJoinRequestResponseDto {

    @Schema(description = "합류 요청 ID", example = "1")
    private Long joinRequestId;

    @Schema(description = "업장명", example = "스타벅스 강남점")
    private String businessName;

    @Schema(description = "합류 요청 상태", example = "PENDING")
    private BusinessJoinRequestStatus status;

    @Schema(description = "합류 요청 일시", example = "2026-03-01T10:00:00")
    private LocalDateTime requestedAt;

    public static MyJoinRequestResponseDto from(BusinessJoinRequest request) {
        return MyJoinRequestResponseDto.builder()
            .joinRequestId(request.getId())
            .businessName(request.getWorkspace().getBusinessName())
            .status(request.getStatus())
            .requestedAt(request.getCreatedAt())
            .build();
    }
}
