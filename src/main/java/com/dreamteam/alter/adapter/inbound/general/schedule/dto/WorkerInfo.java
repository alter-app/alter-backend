package com.dreamteam.alter.adapter.inbound.general.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "근무자 정보")
public class WorkerInfo {

    @Schema(description = "근무자 ID", example = "1")
    private Long workerId;

    @Schema(description = "근무자 이름", example = "홍길동")
    private String workerName;

    @Schema(description = "근무자 프로필 이미지 URL (미설정 시 null)", example = "https://cdn.alter-app.com/users/1/profile.png")
    private String profileImageUrl;

    public static WorkerInfo of(
        Long workerId,
        String workerName,
        String profileImageUrl
    ) {
        return WorkerInfo.builder()
            .workerId(workerId)
            .workerName(workerName)
            .profileImageUrl(profileImageUrl)
            .build();
    }

}
