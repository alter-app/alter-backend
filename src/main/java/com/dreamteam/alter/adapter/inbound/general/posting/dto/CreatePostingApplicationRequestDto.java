package com.dreamteam.alter.adapter.inbound.general.posting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "공고 지원 요청 DTO")
public class CreatePostingApplicationRequestDto {

    @NotNull
    @Schema(description = "지원하고자 하는 공고의 스케줄 ID", example = "1")
    private Long postingScheduleId;

    @NotBlank
    @Schema(description = "지원자 설명", example = "잘 부탁드립니다.")
    private String description;

}
