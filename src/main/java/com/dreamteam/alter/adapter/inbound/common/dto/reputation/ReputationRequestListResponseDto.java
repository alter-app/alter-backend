package com.dreamteam.alter.adapter.inbound.common.dto.reputation;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.adapter.outbound.reputation.persistence.readonly.ReputationRequestListResponse;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestStatus;
import com.dreamteam.alter.domain.reputation.type.ReputationRequestType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "평판 요청 목록 응답 DTO")
public class ReputationRequestListResponseDto {

    @NotNull
    @Schema(description = "평판 요청 ID")
    private Long id;

    @NotNull
    @Schema(description = "평판 요청 타입")
    private ReputationRequestType requestType;

    @NotNull
    @Schema(description = "요청자 정보")
    private RequesterInfo requester;

    @NotNull
    @Schema(description = "대상 정보")
    private TargetInfo target;

    @NotNull
    @Schema(description = "평판 요청 상태")
    private DescribedEnumDto<ReputationRequestStatus> status;

    @NotNull
    @Schema(description = "요청 생성 시간")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @NotNull
    @Schema(description = "요청 만료 시간")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    public static ReputationRequestListResponseDto of(ReputationRequestListResponse entity) {
        return ReputationRequestListResponseDto.builder()
            .id(entity.getId())
            .requestType(entity.getRequestType())
            .requester(RequesterInfo.of(
                entity.getRequesterType(),
                entity.getRequesterId(),
                entity.getRequesterName()
            ))
            .target(TargetInfo.of(
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getTargetName()
            ))
            .status(DescribedEnumDto.of(entity.getStatus(), ReputationRequestStatus.describe()))
            .createdAt(entity.getCreatedAt())
            .expiresAt(entity.getExpiresAt())
            .build();
    }
}
