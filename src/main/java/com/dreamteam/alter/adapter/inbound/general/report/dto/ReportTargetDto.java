package com.dreamteam.alter.adapter.inbound.general.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "신고 대상 정보 DTO")
public class ReportTargetDto {

    @Schema(description = "신고 대상 ID", example = "1")
    private Long targetId;

    @Schema(description = "신고 대상 이름", example = "홍길동")
    private String targetName;

    public static ReportTargetDto of(Long targetId, String targetName) {
        return ReportTargetDto.builder()
                .targetId(targetId)
                .targetName(targetName)
                .build();
    }
}
