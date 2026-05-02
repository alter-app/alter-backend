package com.dreamteam.alter.adapter.inbound.general.user.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.reputation.SelfReputationSummaryDto;
import com.dreamteam.alter.domain.user.result.GetUserSelfInfoResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "마이페이지 사용자 간략 정보 응답 DTO")
public class UserSelfInfoResponseDto {

    @NotNull
    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "사용자 이름", example = "김철수")
    private String name;

    @NotBlank
    @Schema(description = "사용자 닉네임", example = "김땡땡")
    private String nickname;

    @NotNull
    @Schema(description = "가입일", example = "2023-10-01T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "자신의 평판 요약 정보")
    private SelfReputationSummaryDto reputationSummary;

    public static UserSelfInfoResponseDto from(GetUserSelfInfoResult result) {
        return UserSelfInfoResponseDto.builder()
            .id(result.id())
            .name(result.name())
            .nickname(result.nickname())
            .createdAt(result.createdAt())
            .reputationSummary(SelfReputationSummaryDto.from(result.reputationSummary()))
            .build();
    }

}
